import { computed, nextTick, reactive, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { getRequestErrorMessage } from "../../../../shared/kernel/errors/request-error";
import { openBlobInWindow } from "../../../../shared/kernel/utils/blob-window";
import { normalizeDocumentImageSrc } from "../../../../shared/kernel/utils/document-data-url";
import { firstSelectedFile, readFileAsDataUrl } from "./tenant-dashboard-file.utils";
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
  assessTenantHomeAmlOrigination,
  createTenantHomeCustomer,
  createTenantHomeLoan,
  fetchTenantHomeAmlStatus,
  fetchTenantHomeGuidelines,
  fetchTenantHomeKycStatus,
  fetchTenantHomePawnTicketDocument,
  fetchTenantHomePawnTicketLabels,
  fetchTenantHomeQuote,
  fetchTenantHomeReportingOverview,
  prefillTenantHomeKycDocument,
  searchTenantHomeCustomers,
  updateTenantHomeAmlStatus,
  updateTenantHomeKycStatus
} from "../../infrastructure/adapters/http-tenant-dashboard.adapter";
import {
  calculateFinanceTrendMax,
  calculateInventoryMax,
  createAmlRiskLevelOptions,
  createAmlStatusOptions,
  createDocumentTypeOptions,
  createEmptyNewCustomerAml,
  createEmptyNewCustomerKyc,
  createEmptyPosition,
  formatCustomerOption,
  getAmlStatusLabel,
  getKycStatusLabel,
  getRiskLevelLabel,
  getTransactionTypeLabel,
  getVerificationModeLabel,
  isRecoverableStartupError,
  matchesCustomerQuery,
  mergeAmlStatus,
  mergeKycDocuments,
  mergeKycStatus,
  toCustomerModel
} from "../../domain/mappers";
import type { TenantHomeCustomerModel, TenantHomePositionModel } from "../../domain/model/tenant-dashboard";
import type { TenantHomeReportingOverviewDto } from "../../infrastructure/dto/tenant-dashboard.dto";

export function useTenantDashboardService({
  authStore,
  t,
  tenantStore
}: {
  authStore: Record<string, unknown>;
  t: (key: string, params?: Record<string, unknown>) => string;
  tenantStore: { selectedTenant: unknown; selectedTenantId: string; hasFeature: (key: string) => boolean };
}) {
  const router = useRouter();
  const selectedTenant = computed(() => tenantStore.selectedTenant);
  const customerQuery = ref("");
  const customers = ref<TenantHomeCustomerModel[]>([]);
  const customerSuggestions = ref<Array<{ value: string; label: string }>>([]);
  const selectedCustomerId = ref("");
  const selectedCustomerOption = ref<{ value?: string } | null>(null);
  const guidelines = ref<Array<{ id: string; label: string; description: string; baseLoanValue: number }>>([]);
  const useNewCustomer = ref(false);
  const createdLoan = ref<Record<string, unknown> | null>(null);
  const loanQuotes = ref<Array<Record<string, unknown>>>([]);
  const errorMessage = ref("");
  const fieldErrors = ref<Array<{ field: string; message: string }>>([]);
  const successMessage = ref("");
  const isLoading = ref(true);
  const isSubmitting = ref(false);
  const isDownloadingTicket = ref(false);
  const isUpdatingKyc = ref(false);
  const isUpdatingAml = ref(false);
  const issuedTicketRef = ref<HTMLElement | null>(null);
  const reportingOverview = ref<TenantHomeReportingOverviewDto | null>(null);
  const reportingError = ref("");

  const newCustomer = reactive({
    customerNumber: "",
    firstName: "",
    lastName: "",
    birthDate: "",
    phone: "",
    email: "",
    wantsDigitalPawnTicket: false,
    street: "",
    postalCode: "",
    city: ""
  });
  const pledgePresentation = reactive({
    thirdPartyPledgorPresentation: false,
    bearerName: "",
    bearerStreet: "",
    bearerPostalCode: "",
    bearerCity: "",
    powerOfAttorneyDocumentDataUrl: ""
  });
  const newCustomerKyc = reactive(createEmptyNewCustomerKyc());
  const newCustomerAml = reactive(createEmptyNewCustomerAml());
  const terms = reactive({
    termMonths: 3,
    manualMonthlyOperatingFee: ""
  });
  const positions = ref<TenantHomePositionModel[]>([createEmptyPosition()]);

  const selectedCustomer = computed(() =>
    customers.value.find((customer) => customer.id === selectedCustomerId.value) ?? null
  );
  const customerOptions = computed(() =>
    customers.value.map((customer) => formatCustomerOption(customer, t))
  );
  const guidelineOptions = computed(() =>
    guidelines.value.map((guideline) => ({
      value: guideline.id,
      label: guideline.label
    }))
  );
  const documentTypeOptions = computed(() => createDocumentTypeOptions(t));
  const amlStatusOptions = computed(() => createAmlStatusOptions(t));
  const amlRiskLevelOptions = computed(() => createAmlRiskLevelOptions(t));
  const providerVerificationAvailable = computed(() => tenantStore.hasFeature("kyc-provider-verification"));
  const documentOcrAvailable = computed(() => tenantStore.hasFeature("kyc-document-ocr"));
  const amlFeatureEnabled = computed(() => tenantStore.hasFeature("aml-compliance"));
  const canPrefillNewCustomerDocument = computed(() => Boolean(newCustomerKyc.documentFrontImageDataUrl));
  const totalLoanValue = computed(() =>
    positions.value.reduce((total, position) => total + Number(position.pledgedValue || 0), 0).toFixed(2)
  );
  const financeTrendMax = computed(() => calculateFinanceTrendMax(reportingOverview.value));
  const inventoryMax = computed(() => calculateInventoryMax(reportingOverview.value));

  function hasRequiredNewCustomerFields() {
    return hasRequiredNewCustomerFieldsState(newCustomer, newCustomerKyc);
  }

  function hasValidDigitalTicketContact() {
    return hasValidDigitalTicketContactState(newCustomer);
  }

  function hasValidPledgorPresentation() {
    return hasValidPledgorPresentationState(pledgePresentation);
  }

  function hasValidNewCustomerAmlState() {
    return hasValidNewCustomerAmlStateForOrigination(newCustomerAml, amlFeatureEnabled.value);
  }

  function hasValidExistingCustomerState() {
    return hasValidExistingCustomerStateForSelection(
      selectedCustomerId.value,
      selectedCustomer.value,
      amlFeatureEnabled.value
    );
  }

  function hasValidCustomerState() {
    if (useNewCustomer.value) {
      return hasRequiredNewCustomerFields()
        && hasValidDigitalTicketContact()
        && hasValidPledgorPresentation()
        && hasValidNewCustomerAmlState();
    }

    return hasValidExistingCustomerState();
  }

  function hasValidPosition(position: {
    ticketGroup: string | number;
    label: string;
    description: string;
    guidelineId: string;
    pledgedValue: string | number;
  }) {
    return hasValidPositionState(position);
  }

  function hasValidManualFeeWhenRequired() {
    return hasValidManualFeeWhenRequiredState(loanQuotes.value, terms.manualMonthlyOperatingFee);
  }

  const canSubmitLoan = computed(() => {
    const hasCustomer = hasValidCustomerState();
    const hasValidPositions = positions.value.every(hasValidPosition);
    const hasValidTerm = Number(terms.termMonths) >= 3;
    const hasManualFeeWhenRequired = hasValidManualFeeWhenRequired();

    return hasCustomer && hasValidPositions && hasValidTerm && hasManualFeeWhenRequired;
  });

  function resetErrorState() {
    errorMessage.value = "";
    fieldErrors.value = [];
  }

  function handleError(error: unknown) {
    errorMessage.value = getRequestErrorMessage(error, t("common.requestFailed"));
    fieldErrors.value = Array.isArray((error as { fieldErrors?: unknown } | undefined)?.fieldErrors)
      ? ((error as { fieldErrors: Array<{ field: string; message: string }> }).fieldErrors)
      : [];
  }

  async function enrichCustomerCompliance(customer: TenantHomeCustomerModel) {
    try {
      const amlStatus = amlFeatureEnabled.value
        ? await fetchTenantHomeAmlStatus(tenantStore.selectedTenantId, String(customer.id))
        : null;
      return amlStatus ? mergeAmlStatus(toCustomerModel(customer), amlStatus) : toCustomerModel(customer);
    } catch {
      return {
        ...customer,
        amlStatus: "UNKNOWN",
        amlRiskLevel: null,
        amlOriginationAllowed: false,
        amlDecisionReason: t("tenantHome.messages.amlLoadFailed"),
        sourceOfFundsChecked: false,
        suspiciousActivityReported: false,
        goamlReference: null
      };
    }
  }

  async function loadReportingOverview() {
    if (!tenantStore.selectedTenantId) {
      reportingOverview.value = null;
      return;
    }

    try {
      reportingError.value = "";
      reportingOverview.value = await fetchTenantHomeReportingOverview(tenantStore.selectedTenantId, 14);
    } catch (error) {
      reportingOverview.value = null;
      reportingError.value = getRequestErrorMessage(error, t("tenantHome.messages.reportingFailed"));
    }
  }

  async function refreshQuote() {
    if (!tenantStore.selectedTenantId) {
      loanQuotes.value = [];
      return;
    }

    const groupedPositions = positions.value
      .filter((position) => Number(position.pledgedValue) > 0 && Number(position.ticketGroup) >= 1)
      .reduce((groups, position) => {
        const ticketGroup = Number(position.ticketGroup);
        if (!groups.has(ticketGroup)) {
          groups.set(ticketGroup, []);
        }
        groups.get(ticketGroup)?.push(position);
        return groups;
      }, new Map<number, typeof positions.value>());

    if (!groupedPositions.size) {
      loanQuotes.value = [];
      return;
    }

    try {
      fieldErrors.value = [];
      loanQuotes.value = await Promise.all(
        [...groupedPositions.entries()]
          .sort(([left], [right]) => left - right)
          .map(async ([ticketGroup, ticketPositions]) => {
            const loanAmount = ticketPositions.reduce(
              (sum, position) => sum + Number(position.pledgedValue || 0),
              0
            );

            const quote = await fetchTenantHomeQuote(
              {
                loanAmount,
                termMonths: Number(terms.termMonths),
                manualMonthlyOperatingFee: terms.manualMonthlyOperatingFee
                  ? Number(terms.manualMonthlyOperatingFee)
                  : null
              }
            );

            return {
              ...quote,
              ticketGroup,
              positionCount: ticketPositions.length,
              totalLoanValue: loanAmount
            };
          })
      );
    } catch (error) {
      handleError(error);
    }
  }

  async function loadContext() {
    if (!tenantStore.selectedTenantId) {
      isLoading.value = false;
      return;
    }

    isLoading.value = true;
    resetErrorState();

    const [guidelineResult, customerResult] = await Promise.allSettled([
      fetchTenantHomeGuidelines(tenantStore.selectedTenantId),
      searchTenantHomeCustomers(tenantStore.selectedTenantId, "")
    ]);

    try {
      guidelines.value = guidelineResult.status === "fulfilled" ? guidelineResult.value : [];
      customers.value = customerResult.status === "fulfilled"
        ? await Promise.all(customerResult.value.map((customer) => enrichCustomerCompliance(toCustomerModel(customer))))
        : [];

      customerSuggestions.value = customerOptions.value;

      const startupErrors = [guidelineResult, customerResult]
        .filter((result) => result.status === "rejected")
        .map((result) => result.reason);

      if (startupErrors.some((error) => isRecoverableStartupError(error))) {
        errorMessage.value = t("tenantHome.messages.partialDataLoaded");
      } else if (startupErrors.length > 0) {
        handleError(startupErrors[0]);
      }

      await loadReportingOverview();
      await refreshQuote();
    } catch (error) {
      handleError(error);
    } finally {
      isLoading.value = false;
    }
  }

  async function searchCustomerSuggestions(event: { query?: string } = {}) {
    if (!tenantStore.selectedTenantId) {
      return;
    }

    try {
      fieldErrors.value = [];
      const query = String(event.query ?? customerQuery.value ?? "");
      customerQuery.value = query;

      if (query.trim().length < 2) {
        customerSuggestions.value = customers.value
          .filter((customer) => matchesCustomerQuery(customer, query))
          .map((customer) => formatCustomerOption(customer, t));
        return;
      }

      const customerResponse = await searchTenantHomeCustomers(tenantStore.selectedTenantId, query);
      customers.value = await Promise.all(customerResponse.map((customer) => enrichCustomerCompliance(toCustomerModel(customer))));
      customerSuggestions.value = customerOptions.value;
    } catch (error) {
      handleError(error);
    }
  }

  function handleCustomerSelection(event: { value?: { value?: string } } | undefined) {
    const option = event?.value ?? selectedCustomerOption.value;
    selectedCustomerOption.value = option && typeof option === "object" ? option : null;
    selectedCustomerId.value = option?.value ?? "";
  }

  async function loadSelectedCustomerKyc() {
    if (!tenantStore.selectedTenantId || !selectedCustomerId.value || useNewCustomer.value) {
      return;
    }

    try {
      const [kycStatus, amlStatus] = await Promise.all([
        fetchTenantHomeKycStatus(tenantStore.selectedTenantId, selectedCustomerId.value),
        amlFeatureEnabled.value
          ? assessTenantHomeAmlOrigination(
              tenantStore.selectedTenantId,
              selectedCustomerId.value,
              { loanAmount: Number(totalLoanValue.value || 0) }
            )
          : Promise.resolve(null)
      ]);
      customers.value = customers.value.map((customer) =>
        customer.id === selectedCustomerId.value
          ? (amlStatus
              ? mergeAmlStatus(mergeKycStatus(customer, kycStatus), amlStatus)
              : mergeKycStatus(customer, kycStatus))
          : customer
      );
    } catch (error) {
      handleError(error);
    }
  }

  function openSelectedCustomerDetails() {
    if (!selectedCustomerId.value) {
      return;
    }

    router.push({ name: "tenant-customer-detail", params: { customerId: selectedCustomerId.value } });
  }

  function addPosition() {
    const nextGroup = positions.value.reduce((max, position) => Math.max(max, Number(position.ticketGroup) || 0), 0) + 1;
    positions.value = [...positions.value, { ...createEmptyPosition(), ticketGroup: nextGroup }];
  }

  function removePosition(index: number) {
    if (positions.value.length === 1) {
      return;
    }
    positions.value = positions.value.filter((_, currentIndex) => currentIndex !== index);
  }

  function applyGuideline(index: number, guidelineId: string) {
    const guideline = guidelines.value.find((item) => item.id === guidelineId);
    positions.value[index].guidelineId = guidelineId;

    if (!guideline) {
      return;
    }

    if (!positions.value[index].label) {
      positions.value[index].label = guideline.label;
    }

    if (!positions.value[index].description) {
      positions.value[index].description = guideline.description;
    }

    if (!positions.value[index].pledgedValue) {
      positions.value[index].pledgedValue = guideline.baseLoanValue;
    }
  }

  async function submitLoan() {
    errorMessage.value = "";
    fieldErrors.value = [];
    successMessage.value = "";

    if (useNewCustomer.value && !hasRequiredManualKycDocumentsState(newCustomerKyc)) {
      errorMessage.value = t("tenantHome.messages.documentImagesRequired");
      fieldErrors.value = [
        { field: "documentFrontImageDataUrl", message: t("tenantHome.messages.documentImagesRequired") },
        { field: "documentBackImageDataUrl", message: t("tenantHome.messages.documentImagesRequired") }
      ];
      return;
    }

    if (!canSubmitLoan.value) {
      errorMessage.value = t("tenantHome.messages.missingRequiredFields");
      return;
    }

    try {
      isSubmitting.value = true;
      let customerId = selectedCustomerId.value;

      if (useNewCustomer.value) {
        const createdCustomer = await createTenantHomeCustomer(tenantStore.selectedTenantId, { ...newCustomer });
        const kycStatus = await updateTenantHomeKycStatus(
          tenantStore.selectedTenantId,
          createdCustomer.id,
          {
            status: "APPROVED",
            verificationMode: "MANUAL",
            verifiedUntil: newCustomerKyc.documentValidUntil,
            documentType: newCustomerKyc.documentType,
            documentNumber: newCustomerKyc.documentNumber,
            documentValidUntil: newCustomerKyc.documentValidUntil,
            documentFrontImageDataUrl: newCustomerKyc.documentFrontImageDataUrl,
            documentBackImageDataUrl: newCustomerKyc.documentBackImageDataUrl,
            decisionNote: "Manuell im Beleihungsprozess geprüft",
            providerName: null,
            providerReference: null,
            providerStatus: null
          }
        );
        const amlStatus = amlFeatureEnabled.value
          ? await updateTenantHomeAmlStatus(
              tenantStore.selectedTenantId,
              createdCustomer.id,
              {
                status: newCustomerAml.status,
                riskLevel: newCustomerAml.riskLevel,
                pepFlag: newCustomerAml.pepFlag,
                sanctionsHit: newCustomerAml.sanctionsHit,
                unusualTransactionFlag: newCustomerAml.unusualTransactionFlag,
                sourceOfFundsChecked: newCustomerAml.sourceOfFundsChecked,
                suspiciousActivityReported: newCustomerAml.suspiciousActivityReported,
                goamlReference: newCustomerAml.goamlReference || null,
                decisionNote: newCustomerAml.decisionNote || null,
                lastScreenedAt: null,
                reviewedAt: null
              }
            )
          : null;
        customerId = createdCustomer.id;
        const mergedCustomer = mergeKycDocuments(mergeKycStatus(createdCustomer, kycStatus), {
          documentFrontImageDataUrl: newCustomerKyc.documentFrontImageDataUrl,
          documentBackImageDataUrl: newCustomerKyc.documentBackImageDataUrl
        });
        customers.value = [amlStatus ? mergeAmlStatus(mergedCustomer, amlStatus) : mergedCustomer, ...customers.value];
      }

      const payload = {
        customerId,
        positions: positions.value.map((position) => ({
          ticketGroup: Number(position.ticketGroup),
          label: position.label,
          description: position.description,
          guidelineId: position.guidelineId,
          pledgedValue: position.pledgedValue ? Number(position.pledgedValue) : null
        })),
        termMonths: Number(terms.termMonths),
        manualMonthlyOperatingFee: terms.manualMonthlyOperatingFee
          ? Number(terms.manualMonthlyOperatingFee)
          : null,
        thirdPartyPledgorPresentation: pledgePresentation.thirdPartyPledgorPresentation,
        bearerName: pledgePresentation.thirdPartyPledgorPresentation ? pledgePresentation.bearerName : null,
        bearerStreet: pledgePresentation.thirdPartyPledgorPresentation ? pledgePresentation.bearerStreet : null,
        bearerPostalCode: pledgePresentation.thirdPartyPledgorPresentation ? pledgePresentation.bearerPostalCode : null,
        bearerCity: pledgePresentation.thirdPartyPledgorPresentation ? pledgePresentation.bearerCity : null,
        powerOfAttorneyDocumentDataUrl: pledgePresentation.thirdPartyPledgorPresentation
          ? pledgePresentation.powerOfAttorneyDocumentDataUrl
          : null
      };

      createdLoan.value = await createTenantHomeLoan(tenantStore.selectedTenantId, payload) as Record<string, unknown>;
      await loadReportingOverview();
      successMessage.value = t("tenantHome.messages.loanCreated");
      positions.value = [createEmptyPosition()];
      selectedCustomerId.value = "";
      selectedCustomerOption.value = null;
      customerQuery.value = "";
      customerSuggestions.value = customerOptions.value;
      useNewCustomer.value = false;
      terms.manualMonthlyOperatingFee = "";
      Object.assign(pledgePresentation, {
        thirdPartyPledgorPresentation: false,
        bearerName: "",
        bearerStreet: "",
        bearerPostalCode: "",
        bearerCity: "",
        powerOfAttorneyDocumentDataUrl: ""
      });
      Object.assign(newCustomerKyc, createEmptyNewCustomerKyc());
      Object.assign(newCustomerAml, createEmptyNewCustomerAml());
      await nextTick();
      issuedTicketRef.value?.scrollIntoView?.({ behavior: "smooth", block: "start" });
    } catch (error) {
      handleError(error);
    } finally {
      isSubmitting.value = false;
    }
  }

  async function updateSelectedCustomerKyc(verificationMode: "MANUAL" | "PROVIDER") {
    if (!tenantStore.selectedTenantId || !selectedCustomerId.value) {
      return;
    }

    try {
      isUpdatingKyc.value = true;
      fieldErrors.value = [];
      if (
        verificationMode === "MANUAL"
        && !hasRequiredManualKycDocumentsState({
          documentFrontImageDataUrl: selectedCustomer.value?.documentFrontImageDataUrl,
          documentBackImageDataUrl: selectedCustomer.value?.documentBackImageDataUrl
        })
      ) {
        errorMessage.value = t("tenantHome.messages.documentImagesRequired");
        fieldErrors.value = [
          { field: "documentFrontImageDataUrl", message: t("tenantHome.messages.documentImagesRequired") },
          { field: "documentBackImageDataUrl", message: t("tenantHome.messages.documentImagesRequired") }
        ];
        return;
      }
      const kycStatus = await updateTenantHomeKycStatus(
        tenantStore.selectedTenantId,
        selectedCustomerId.value,
        verificationMode === "PROVIDER"
          ? {
              status: "IN_PROGRESS",
              verificationMode: "PROVIDER",
              verifiedUntil: null,
              documentType: "PERSONALAUSWEIS",
              decisionNote: "Provider-Prüfung im Tenant-Dashboard vorgemerkt",
              providerName: "configured-provider",
              providerReference: `provider-${selectedCustomerId.value}`,
              providerStatus: "PENDING"
            }
          : {
              status: "APPROVED",
              verificationMode: "MANUAL",
              verifiedUntil: selectedCustomer.value?.documentValidUntil,
              documentType: selectedCustomer.value?.documentType,
              documentNumber: selectedCustomer.value?.documentNumber,
              documentValidUntil: selectedCustomer.value?.documentValidUntil,
              documentFrontImageDataUrl: selectedCustomer.value?.documentFrontImageDataUrl,
              documentBackImageDataUrl: selectedCustomer.value?.documentBackImageDataUrl,
              decisionNote: "Manuell im Tenant-Dashboard freigegeben",
              providerName: null,
              providerReference: null,
              providerStatus: null
            }
      );

      customers.value = customers.value.map((customer) =>
        customer.id === selectedCustomerId.value
          ? mergeKycStatus(customer, kycStatus)
          : customer
      );
    } catch (error) {
      handleError(error);
    } finally {
      isUpdatingKyc.value = false;
    }
  }

  function approveSelectedCustomerKyc() {
    return updateSelectedCustomerKyc("MANUAL");
  }

  function startProviderVerification() {
    return updateSelectedCustomerKyc("PROVIDER");
  }

  async function prefillNewCustomerDocumentData() {
    if (!tenantStore.selectedTenantId || !newCustomerKyc.documentFrontImageDataUrl) {
      return;
    }

    try {
      resetErrorState();
      const result = await prefillTenantHomeKycDocument(
        tenantStore.selectedTenantId,
        "new-customer",
        {
          documentFrontImageDataUrl: newCustomerKyc.documentFrontImageDataUrl,
          documentBackImageDataUrl: newCustomerKyc.documentBackImageDataUrl
        }
      );

      if (!result.available || !result.matched) {
        errorMessage.value = t("customerDetail.messages.ocrUnavailable");
        return;
      }

      newCustomerKyc.documentType = result.documentType ?? newCustomerKyc.documentType;
      newCustomerKyc.documentNumber = result.documentNumber ?? newCustomerKyc.documentNumber;
      newCustomerKyc.documentValidUntil = result.documentValidUntil ?? newCustomerKyc.documentValidUntil;
      newCustomerKyc.portraitImageDataUrl = result.portraitImageDataUrl ?? newCustomerKyc.portraitImageDataUrl;

      if (result.firstName) newCustomer.firstName = result.firstName;
      if (result.lastName) newCustomer.lastName = result.lastName;
      if (result.birthDate) newCustomer.birthDate = result.birthDate;
    } catch (error) {
      handleError(error);
    }
  }

  async function updateNewCustomerDocument(side: "documentFrontImageDataUrl" | "documentBackImageDataUrl", event: unknown) {
    const file = firstSelectedFile(event);
    if (!file) {
      newCustomerKyc[side] = "";
      return;
    }

    try {
      newCustomerKyc[side] = await readFileAsDataUrl(file, t);

      if (documentOcrAvailable.value && newCustomerKyc.documentFrontImageDataUrl) {
        await prefillNewCustomerDocumentData();
      }
    } catch (error) {
      handleError(error);
    }
  }

  async function updatePowerOfAttorneyDocument(event: unknown) {
    const file = firstSelectedFile(event);
    if (!file) {
      pledgePresentation.powerOfAttorneyDocumentDataUrl = "";
      return;
    }

    try {
      pledgePresentation.powerOfAttorneyDocumentDataUrl = await readFileAsDataUrl(file, t);
    } catch (error) {
      handleError(error);
    }
  }

  function clearNewCustomerDocument(side: "documentFrontImageDataUrl" | "documentBackImageDataUrl") {
    newCustomerKyc[side] = "";
  }

  function clearPowerOfAttorneyDocument() {
    pledgePresentation.powerOfAttorneyDocumentDataUrl = "";
  }

  async function openPawnTicketDocument(ticketNumber: string, printMode = false) {
    if (!ticketNumber) {
      return;
    }

    try {
      isDownloadingTicket.value = true;
      const blob = await fetchTenantHomePawnTicketDocument(ticketNumber);
      openBlobInWindow(blob, { printMode });
    } catch (error) {
      handleError(error);
    } finally {
      isDownloadingTicket.value = false;
    }
  }

  async function openPawnTicketLabels(ticketNumber: string, printMode = false) {
    if (!ticketNumber) {
      return;
    }

    try {
      isDownloadingTicket.value = true;
      const blob = await fetchTenantHomePawnTicketLabels(ticketNumber);
      openBlobInWindow(blob, { printMode });
    } catch (error) {
      handleError(error);
    } finally {
      isDownloadingTicket.value = false;
    }
  }

  watch([positions, () => terms.termMonths, () => terms.manualMonthlyOperatingFee], refreshQuote, { deep: true });
  watch([selectedCustomerId, totalLoanValue], loadSelectedCustomerKyc);
  watch(selectedCustomerOption, (option) => {
    if (!option || typeof option !== "object") {
      selectedCustomerId.value = "";
      return;
    }

    selectedCustomerId.value = option.value ?? "";
  });

  return {
    addPosition,
    applyGuideline,
    approveSelectedCustomerKyc,
    amlFeatureEnabled,
    amlRiskLevelOptions,
    amlStatusOptions,
    canPrefillNewCustomerDocument,
    canSubmitLoan,
    clearNewCustomerDocument,
    clearPowerOfAttorneyDocument,
    createdLoan,
    customerQuery,
    customerSuggestions,
    customerOptions,
    customers,
    documentOcrAvailable,
    documentTypeOptions,
    errorMessage,
    fieldErrors,
    financeTrendMax,
    getAmlStatusLabel: (status: string | null | undefined) => getAmlStatusLabel(t, status),
    getKycStatusLabel: (status: string | null | undefined) => getKycStatusLabel(t, status),
    getRiskLevelLabel: (level: string | null | undefined) => getRiskLevelLabel(t, level),
    getTransactionTypeLabel: (type: string | null | undefined) => getTransactionTypeLabel(t, type),
    getVerificationModeLabel: (mode: string | null | undefined) => getVerificationModeLabel(t, mode),
    guidelineOptions,
    guidelines,
    handleCustomerSelection,
    inventoryMax,
    isDownloadingTicket,
    isLoading,
    isSubmitting,
    isUpdatingAml,
    isUpdatingKyc,
    issuedTicketRef,
    loadContext,
    loanQuotes,
    newCustomer,
    newCustomerAml,
    newCustomerKyc,
    openPawnTicketDocument,
    openPawnTicketLabels,
    openSelectedCustomerDetails,
    positions,
    prefillNewCustomerDocumentData,
    pledgePresentation,
    providerVerificationAvailable,
    reportingError,
    reportingOverview,
    removePosition,
    resolveDocumentImageSrc: (value: string) => normalizeDocumentImageSrc(value),
    searchCustomerSuggestions,
    selectedCustomer,
    selectedCustomerId,
    selectedCustomerOption,
    selectedTenant,
    startProviderVerification,
    submitLoan,
    successMessage,
    terms,
    totalLoanValue,
    updateNewCustomerDocument,
    updatePowerOfAttorneyDocument,
    useNewCustomer
  };
}
