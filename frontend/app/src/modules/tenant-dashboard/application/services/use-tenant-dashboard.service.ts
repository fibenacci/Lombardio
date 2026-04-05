import { computed, nextTick, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { getRequestErrorMessage } from "../../../../shared/kernel/errors/request-error";
import { openBlobInWindow } from "../../../../shared/kernel/utils/blob-window";
import { normalizeDocumentImageSrc } from "../../../../shared/kernel/utils/document-data-url";
import { 
  hasRequiredManualKycDocuments as hasRequiredManualKycDocumentsState,
  hasRequiredNewCustomerFields as hasRequiredNewCustomerFieldsState,
  hasValidDigitalTicketContact as hasValidDigitalTicketContactState,
  hasValidExistingCustomerState as hasValidExistingCustomerStateForSelection,
  hasValidManualFeeWhenRequired as hasValidManualFeeWhenRequiredState,
  hasValidNewCustomerAmlState as hasValidNewCustomerAmlStateForOrigination,
  hasValidPledgorPresentation as hasValidPledgorPresentationState,
  hasValidPosition as hasValidPositionState
} from "./tenant-dashboard-validation";
import {
  createTenantHomeCustomer,
  createTenantHomeLoan,
  fetchTenantHomeAmlStatus,
  fetchTenantHomePawnTicketDocument,
  fetchTenantHomePawnTicketLabels,
  updateTenantHomeAmlStatus,
  updateTenantHomeKycStatus
} from "../../infrastructure/adapters/http-tenant-dashboard.adapter";
import {
  createAmlRiskLevelOptions,
  createAmlStatusOptions,
  createDocumentTypeOptions,
  createEmptyPosition,
  getAmlStatusLabel,
  getKycStatusLabel,
  getRiskLevelLabel,
  getTransactionTypeLabel,
  getVerificationModeLabel,
  mergeAmlStatus,
  mergeKycDocuments,
  mergeKycStatus,
  toCustomerModel
} from "../../domain/mappers";
import { useTenantDashboardReporting } from "./sub-composables/use-tenant-dashboard-reporting";
import { useTenantDashboardCustomerLookup } from "./sub-composables/use-tenant-dashboard-customer-lookup";
import { useTenantDashboardLoanForm } from "./sub-composables/use-tenant-dashboard-loan-form";
import { useTenantDashboardCompliance } from "./sub-composables/use-tenant-dashboard-compliance";
import type { TenantHomeCustomerModel } from "../../domain/model/tenant-dashboard";
import type { TenantHomeLoanDto } from "../../infrastructure/dto/tenant-dashboard.dto";
import type { FieldError } from "../../../../shared/kernel/http/types";

type ErrorWithFieldErrors = {
  fieldErrors?: unknown;
};

type TenantHomeOptionLabelValue = {
  label: string;
  value: string;
};

type TenantStatusValue = string | null | undefined;

function isFieldError(value: unknown): value is FieldError {
  return typeof value === "object"
    && value !== null
    && "field" in value
    && typeof value.field === "string"
    && "message" in value
    && typeof value.message === "string";
}

export function useTenantDashboardService({
  t,
  tenantStore
}: {
  t: (key: string, params?: Record<string, unknown>) => string;
  tenantStore: { selectedTenant: unknown; selectedTenantId: string; hasFeature: (key: string) => boolean };
}) {
  const router = useRouter();
  const selectedTenantId = computed(() => tenantStore.selectedTenantId);
  const selectedTenant = computed(() => tenantStore.selectedTenant);
  
  const errorMessage = ref("");
  const fieldErrors = ref<FieldError[]>([]);
  const successMessage = ref("");
  const isLoading = ref(true);
  const isSubmitting = ref(false);
  const isDownloadingTicket = ref(false);
  const createdLoan = ref<Record<string, unknown> | null>(null);
  const issuedTicketRef = ref<HTMLElement | null>(null);

  const amlFeatureEnabled = computed(() => tenantStore.hasFeature("aml-compliance"));
  const documentOcrAvailable = computed(() => tenantStore.hasFeature("kyc-document-ocr"));
  const providerVerificationAvailable = computed(() => tenantStore.hasFeature("kyc-provider-verification"));

  const complianceCustomers = ref<TenantHomeCustomerModel[]>([]);

  // Sub-Composables
  const reporting = useTenantDashboardReporting({ tenantId: selectedTenantId, t });

  const compliance = useTenantDashboardCompliance({
    tenantId: selectedTenantId,
    t,
    amlFeatureEnabled,
    customers: complianceCustomers
  });

  const enrichCompliance = async (customer: TenantHomeCustomerModel) => {
    try {
      const amlStatus = amlFeatureEnabled.value
        ? await fetchTenantHomeAmlStatus(selectedTenantId.value, String(customer.id))
        : null;
      return amlStatus ? mergeAmlStatus(toCustomerModel(customer), amlStatus) : toCustomerModel(customer);
    } catch {
      return {
        ...customer,
        amlStatus: "UNKNOWN",
        amlOriginationAllowed: false
      } as TenantHomeCustomerModel;
    }
  };

  const customerLookup = useTenantDashboardCustomerLookup({
    tenantId: selectedTenantId,
    t,
    ocrAvailable: documentOcrAvailable,
    onEnrichCompliance: enrichCompliance,
    customers: complianceCustomers
  });

  const loanForm = useTenantDashboardLoanForm({ tenantId: selectedTenantId, t });

  const canSubmitLoan = computed(() => {
    const hasCustomer = customerLookup.useNewCustomer.value 
      ? (hasRequiredNewCustomerFieldsState(customerLookup.newCustomer, customerLookup.newCustomerKyc) &&
         hasValidDigitalTicketContactState(customerLookup.newCustomer) &&
         hasValidPledgorPresentationState(loanForm.pledgePresentation) &&
         hasValidNewCustomerAmlStateForOrigination(customerLookup.newCustomerAml, amlFeatureEnabled.value))
      : hasValidExistingCustomerStateForSelection(
          customerLookup.selectedCustomerId.value,
          customerLookup.selectedCustomer.value,
          amlFeatureEnabled.value
        );

    const hasValidPositions = loanForm.positions.value.every(hasValidPositionState);
    const hasValidTerm = Number(loanForm.terms.termMonths) >= 3;
    const hasManualFeeWhenRequired = hasValidManualFeeWhenRequiredState(loanForm.loanQuotes.value, loanForm.terms.manualMonthlyOperatingFee);

    return hasCustomer && hasValidPositions && hasValidTerm && hasManualFeeWhenRequired;
  });

  async function loadContext() {
    if (!selectedTenantId.value) {
      isLoading.value = false;
      return;
    }

    isLoading.value = true;
    errorMessage.value = "";
    fieldErrors.value = [];

    try {
      await Promise.all([
        loanForm.loadGuidelines(),
        customerLookup.searchCustomerSuggestions(),
        reporting.loadReportingOverview()
      ]);
      await loanForm.refreshQuote();
    } catch (error) {
      handleError(error);
    } finally {
      isLoading.value = false;
    }
  }

  async function submitLoan() {
    errorMessage.value = "";
    fieldErrors.value = [];
    successMessage.value = "";

    if (customerLookup.useNewCustomer.value && !hasRequiredManualKycDocumentsState(customerLookup.newCustomerKyc)) {
      errorMessage.value = t("tenantHome.messages.documentImagesRequired");
      return;
    }

    if (!canSubmitLoan.value) {
      errorMessage.value = t("tenantHome.messages.missingRequiredFields");
      return;
    }

    try {
      isSubmitting.value = true;
      let customerId = customerLookup.selectedCustomerId.value;

      if (customerLookup.useNewCustomer.value) {
        const createdCustomer = await createTenantHomeCustomer(selectedTenantId.value, { ...customerLookup.newCustomer });
        const kycStatus = await updateTenantHomeKycStatus(selectedTenantId.value, createdCustomer.id, {
          status: "APPROVED",
          verificationMode: "MANUAL",
          verifiedUntil: customerLookup.newCustomerKyc.documentValidUntil,
          documentType: customerLookup.newCustomerKyc.documentType,
          documentNumber: customerLookup.newCustomerKyc.documentNumber,
          documentValidUntil: customerLookup.newCustomerKyc.documentValidUntil,
          documentFrontImageDataUrl: customerLookup.newCustomerKyc.documentFrontImageDataUrl,
          documentBackImageDataUrl: customerLookup.newCustomerKyc.documentBackImageDataUrl,
          decisionNote: "Manuell im Beleihungsprozess geprüft",
          providerName: null,
          providerReference: null,
          providerStatus: null
        });
        
        const amlStatus = amlFeatureEnabled.value
          ? await updateTenantHomeAmlStatus(selectedTenantId.value, createdCustomer.id, {
              ...customerLookup.newCustomerAml,
              goamlReference: customerLookup.newCustomerAml.goamlReference || null,
              decisionNote: customerLookup.newCustomerAml.decisionNote || null,
              lastScreenedAt: null,
              reviewedAt: null
            })
          : null;

        customerId = createdCustomer.id;
        const mergedCustomer = mergeKycDocuments(mergeKycStatus(createdCustomer, kycStatus), {
          documentFrontImageDataUrl: customerLookup.newCustomerKyc.documentFrontImageDataUrl,
          documentBackImageDataUrl: customerLookup.newCustomerKyc.documentBackImageDataUrl
        });
        customerLookup.customers.value = [amlStatus ? mergeAmlStatus(mergedCustomer, amlStatus) : mergedCustomer, ...customerLookup.customers.value];
      }

      const payload = {
        customerId,
        positions: loanForm.positions.value.map((p) => ({
          ...p,
          pledgedValue: p.pledgedValue ? Number(p.pledgedValue) : null
        })),
        termMonths: Number(loanForm.terms.termMonths),
        manualMonthlyOperatingFee: loanForm.terms.manualMonthlyOperatingFee ? Number(loanForm.terms.manualMonthlyOperatingFee) : null,
        thirdPartyPledgorPresentation: loanForm.pledgePresentation.thirdPartyPledgorPresentation,
        bearerName: loanForm.pledgePresentation.bearerName,
        bearerStreet: loanForm.pledgePresentation.bearerStreet,
        bearerPostalCode: loanForm.pledgePresentation.bearerPostalCode,
        bearerCity: loanForm.pledgePresentation.bearerCity,
        powerOfAttorneyDocumentDataUrl: loanForm.pledgePresentation.powerOfAttorneyDocumentDataUrl
      };

      createdLoan.value = await createTenantHomeLoan(selectedTenantId.value, payload) as TenantHomeLoanDto & Record<string, unknown>;
      await reporting.loadReportingOverview();
      
      successMessage.value = t("tenantHome.messages.loanCreated");
      
      // Reset form
      loanForm.positions.value = [createEmptyPosition()];
      customerLookup.selectedCustomerId.value = "";
      customerLookup.selectedCustomerOption.value = null;
      customerLookup.useNewCustomer.value = false;
      
      await nextTick();
      issuedTicketRef.value?.scrollIntoView?.({ behavior: "smooth", block: "start" });
    } catch (error) {
      handleError(error);
    } finally {
      isSubmitting.value = false;
    }
  }

  function handleError(error: unknown) {
    const errorWithFieldErrors = error as ErrorWithFieldErrors;
    errorMessage.value = getRequestErrorMessage(error, t("common.requestFailed"));
    fieldErrors.value = Array.isArray(errorWithFieldErrors?.fieldErrors)
      ? errorWithFieldErrors.fieldErrors.filter(isFieldError)
      : [];
  }

  async function openPawnTicketDocument(ticketNumber: string, printMode = false) {
    if (!ticketNumber) return;
    try {
      isDownloadingTicket.value = true;
      const blob = await fetchTenantHomePawnTicketDocument(ticketNumber);
      openBlobInWindow(blob, { printMode });
    } finally {
      isDownloadingTicket.value = false;
    }
  }

  watch([loanForm.positions, () => loanForm.terms.termMonths, () => loanForm.terms.manualMonthlyOperatingFee], loanForm.refreshQuote, { deep: true });
  watch([customerLookup.selectedCustomerId, loanForm.totalLoanValue], () => 
    compliance.loadSelectedCustomerKyc(customerLookup.selectedCustomerId.value, Number(loanForm.totalLoanValue.value))
  );

  return {
    ...reporting,
    ...customerLookup,
    ...loanForm,
    ...compliance,
    amlFeatureEnabled,
    documentOcrAvailable,
    providerVerificationAvailable,
    errorMessage,
    fieldErrors,
    successMessage,
    isLoading,
    isSubmitting,
    isDownloadingTicket,
    createdLoan,
    issuedTicketRef,
    loadContext,
    submitLoan,
    openPawnTicketDocument,
    openPawnTicketLabels: async (num: string, print = false) => {
      isDownloadingTicket.value = true;
      const blob = await fetchTenantHomePawnTicketLabels(num);
      openBlobInWindow(blob, { printMode: print });
      isDownloadingTicket.value = false;
    },
    openSelectedCustomerDetails: () => {
      if (customerLookup.selectedCustomerId.value) {
        router.push({ name: "tenant-customer-detail", params: { customerId: customerLookup.selectedCustomerId.value } });
      }
    },
    resolveDocumentImageSrc: (v: string) => normalizeDocumentImageSrc(v),
    getAmlStatusLabel: (s: TenantStatusValue) => getAmlStatusLabel(t, s),
    getKycStatusLabel: (s: TenantStatusValue) => getKycStatusLabel(t, s),
    getRiskLevelLabel: (l: TenantStatusValue) => getRiskLevelLabel(t, l),
    getTransactionTypeLabel: (ty: TenantStatusValue) => getTransactionTypeLabel(t, ty),
    getVerificationModeLabel: (m: TenantStatusValue) => getVerificationModeLabel(t, m),
    documentTypeOptions: computed<TenantHomeOptionLabelValue[]>(() => createDocumentTypeOptions(t)),
    amlStatusOptions: computed<TenantHomeOptionLabelValue[]>(() => createAmlStatusOptions(t)),
    amlRiskLevelOptions: computed<TenantHomeOptionLabelValue[]>(() => createAmlRiskLevelOptions(t)),
    approveSelectedCustomerKyc: async () => {
      if (customerLookup.selectedCustomerId.value) {
        await compliance.updateSelectedCustomerKyc(
          customerLookup.selectedCustomerId.value,
          "MANUAL",
          customerLookup.selectedCustomer.value
        );
      }
    },
    startProviderVerification: async () => {
      if (customerLookup.selectedCustomerId.value) {
        await compliance.startProviderVerification(customerLookup.selectedCustomerId.value);
      }
    },
    canSubmitLoan,
    selectedTenant
  };
}
