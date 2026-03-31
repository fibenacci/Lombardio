import { computed, defineComponent, nextTick, onMounted, reactive, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../../stores/auth";
import { useTenantStore } from "../../stores/tenant";
import { createLoan, fetchValuationGuidelines } from "../../services/api/origination";
import { createCustomer, searchCustomers } from "../../services/api/customer";
import { assessAmlOrigination, fetchAmlStatus, updateAmlStatus } from "../../services/api/aml";
import { fetchKycStatus, prefillKycDocument, updateKycStatus } from "../../services/api/kyc";
import { fetchPawnTicketDocument, fetchPawnTicketLabels, fetchPawnTicketQuote } from "../../services/api/pawnTicket";
import { fetchDashboardOverview } from "../../services/api/reporting";
import { useI18n } from "../../i18n";
import FormFeedback from "../../components/form-feedback";
import template from "./template.html?raw";
import "./styles.scss";

function createEmptyNewCustomerKyc() {
  return {
    documentType: "PERSONALAUSWEIS",
    documentNumber: "",
    documentValidUntil: "",
    documentFrontImageDataUrl: "",
    documentBackImageDataUrl: "",
    portraitImageDataUrl: ""
  };
}

function createEmptyPosition() {
  return {
    ticketGroup: 1,
    label: "",
    description: "",
    guidelineId: "",
    pledgedValue: ""
  };
}

function mergeKycStatus(customer, kycStatus) {
  return {
    ...customer,
    verificationMode: kycStatus.verificationMode,
    kycStatus: kycStatus.status,
    verifiedUntil: kycStatus.verifiedUntil,
    documentType: kycStatus.documentType,
    documentNumber: kycStatus.documentNumber,
    documentValidUntil: kycStatus.documentValidUntil,
    documentFrontImageDataUrl: kycStatus.documentFrontImageDataUrl,
    documentBackImageDataUrl: kycStatus.documentBackImageDataUrl,
    decisionNote: kycStatus.decisionNote,
    providerName: kycStatus.providerName,
    providerReference: kycStatus.providerReference,
    providerStatus: kycStatus.providerStatus,
    kycApproved: kycStatus.status === "APPROVED"
  };
}

function mergeAmlStatus(customer, amlStatus) {
  return {
    ...customer,
    amlStatus: amlStatus.status,
    amlRiskLevel: amlStatus.riskLevel,
    amlOriginationAllowed: amlStatus.originationAllowed,
    amlDecisionReason: amlStatus.decisionReason,
    sourceOfFundsChecked: amlStatus.sourceOfFundsChecked,
    suspiciousActivityReported: amlStatus.suspiciousActivityReported,
    goamlReference: amlStatus.goamlReference
  };
}

function createEmptyNewCustomerAml() {
  return {
    status: "CLEAR",
    riskLevel: "MEDIUM",
    pepFlag: false,
    sanctionsHit: false,
    unusualTransactionFlag: false,
    sourceOfFundsChecked: false,
    suspiciousActivityReported: false,
    goamlReference: "",
    decisionNote: ""
  };
}

function readFileAsDataUrl(file, t) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(typeof reader.result === "string" ? reader.result : "");
    reader.onerror = () => reject(new Error(t("common.fileReadFailed")));
    reader.readAsDataURL(file);
  });
}

function formatCurrency(value) {
  return new Intl.NumberFormat("de-DE", {
    style: "currency",
    currency: "EUR"
  }).format(Number(value ?? 0));
}

function formatCustomerOption(customer, t) {
  const kycStatus = customer.kycStatus ?? "NOT_STARTED";
  return {
    value: customer.id,
    label: t("tenantHome.customerOption", {
      customerNumber: customer.customerNumber ?? "",
      displayName: customer.displayName ?? `${customer.firstName ?? ""} ${customer.lastName ?? ""}`.trim(),
      kycStatus: t(`customerDetail.statusOptions.kyc.${kycStatus}`)
    })
  };
}

function matchesCustomerQuery(customer, query) {
  const normalizedQuery = query.trim().toLowerCase();

  if (!normalizedQuery) {
    return true;
  }

  return [
    customer.customerNumber,
    customer.displayName,
    customer.firstName,
    customer.lastName,
    customer.phone
  ]
    .filter((value) => String(value ?? "").trim().length > 0)
    .some((value) => String(value).toLowerCase().includes(normalizedQuery));
}

function isRecoverableStartupError(error) {
  return [502, 503, 504].includes(Number(error?.status));
}

export default defineComponent({
  name: "TenantHomeView",
  setup() {
    const router = useRouter();
    const { t } = useI18n();
    const authStore = useAuthStore();
    const tenantStore = useTenantStore();
    const selectedTenant = computed(() => tenantStore.selectedTenant);
    const customerQuery = ref("");
    const customers = ref([]);
    const customerSuggestions = ref([]);
    const customerOptions = computed(() =>
      customers.value.map((customer) => formatCustomerOption(customer, t))
    );
    const selectedCustomerId = ref("");
    const selectedCustomerOption = ref(null);
    const selectedCustomer = computed(() =>
      customers.value.find((customer) => customer.id === selectedCustomerId.value) ?? null
    );
    const guidelines = ref([]);
    const guidelineOptions = computed(() =>
      guidelines.value.map((guideline) => ({
        value: guideline.id,
        label: guideline.label
      }))
    );
    const documentTypeOptions = computed(() => [
      { value: "PERSONALAUSWEIS", label: t("customerDetail.documentTypeOptions.PERSONALAUSWEIS") },
      { value: "REISEPASS", label: t("customerDetail.documentTypeOptions.REISEPASS") },
      { value: "AUFENTHALTSTITEL", label: t("customerDetail.documentTypeOptions.AUFENTHALTSTITEL") }
    ]);
    const amlStatusOptions = computed(() => [
      { value: "NOT_REVIEWED", label: t("customerDetail.statusOptions.aml.NOT_REVIEWED") },
      { value: "CLEAR", label: t("customerDetail.statusOptions.aml.CLEAR") },
      { value: "REVIEW_REQUIRED", label: t("customerDetail.statusOptions.aml.REVIEW_REQUIRED") },
      { value: "BLOCKED", label: t("customerDetail.statusOptions.aml.BLOCKED") },
      { value: "REPORTED", label: t("customerDetail.statusOptions.aml.REPORTED") }
    ]);
    const amlRiskLevelOptions = computed(() => [
      { value: "LOW", label: t("customerDetail.riskLevels.LOW") },
      { value: "MEDIUM", label: t("customerDetail.riskLevels.MEDIUM") },
      { value: "HIGH", label: t("customerDetail.riskLevels.HIGH") }
    ]);
    const useNewCustomer = ref(false);
    const createdLoan = ref(null);
    const loanQuotes = ref([]);
    const errorMessage = ref("");
    const fieldErrors = ref([]);
    const successMessage = ref("");
    const isLoading = ref(true);
    const isSubmitting = ref(false);
    const isDownloadingTicket = ref(false);
    const isUpdatingKyc = ref(false);
    const isUpdatingAml = ref(false);
    const issuedTicketRef = ref(null);
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
    const positions = ref([createEmptyPosition()]);
    const reportingOverview = ref(null);
    const reportingError = ref("");

    const canSubmitLoan = computed(() => {
      const hasCustomer = useNewCustomer.value
        ? [
            newCustomer.customerNumber,
            newCustomer.firstName,
            newCustomer.lastName,
            newCustomer.birthDate,
            newCustomer.phone,
            newCustomerKyc.documentType,
            newCustomerKyc.documentNumber,
            newCustomerKyc.documentValidUntil,
            newCustomerKyc.documentFrontImageDataUrl,
            newCustomerKyc.documentBackImageDataUrl
          ]
            .every((value) => String(value ?? "").trim().length > 0)
            && (!newCustomer.wantsDigitalPawnTicket || String(newCustomer.email ?? "").trim().length > 0)
            && (!pledgePresentation.thirdPartyPledgorPresentation
              || (
                String(pledgePresentation.bearerName ?? "").trim().length > 0
                && String(pledgePresentation.powerOfAttorneyDocumentDataUrl ?? "").trim().length > 0
              ))
            && (!amlFeatureEnabled.value
              || (
                newCustomerAml.status === "CLEAR"
                && !newCustomerAml.sanctionsHit
                && !newCustomerAml.unusualTransactionFlag
                && (!newCustomerAml.suspiciousActivityReported || String(newCustomerAml.goamlReference ?? "").trim().length > 0)
                && (!(newCustomerAml.pepFlag && newCustomerAml.riskLevel === "HIGH") || newCustomerAml.sourceOfFundsChecked)
              ))
        : String(selectedCustomerId.value ?? "").trim().length > 0
          && selectedCustomer.value?.kycApproved === true
          && (!amlFeatureEnabled.value || selectedCustomer.value?.amlOriginationAllowed === true);

      const hasValidPositions = positions.value.every((position) =>
        Number(position.ticketGroup) >= 1
        && Number.isInteger(Number(position.ticketGroup))
        && String(position.label ?? "").trim().length > 0
        && String(position.description ?? "").trim().length > 0
        && String(position.guidelineId ?? "").trim().length > 0
        && Number(position.pledgedValue) > 0
      );

      const hasValidTerm = Number(terms.termMonths) >= 3;
      const hasManualFeeWhenRequired = !loanQuotes.value.some((quote) => quote.manualMonthlyOperatingFeeRequired)
        || Number(terms.manualMonthlyOperatingFee) >= 0;

      return hasCustomer && hasValidPositions && hasValidTerm && hasManualFeeWhenRequired;
    });

    const totalLoanValue = computed(() =>
      positions.value.reduce((total, position) => total + Number(position.pledgedValue || 0), 0).toFixed(2)
    );
    const providerVerificationAvailable = computed(() => tenantStore.hasFeature("kyc-provider-verification"));
    const documentOcrAvailable = computed(() => tenantStore.hasFeature("kyc-document-ocr"));
    const amlFeatureEnabled = computed(() => tenantStore.hasFeature("aml-compliance"));
    const financeTrendMax = computed(() => {
      const values = reportingOverview.value?.financeTrend?.flatMap((point) => [
        Number(point.cashInflow ?? 0),
        Number(point.cashOutflow ?? 0),
        Number(point.realizedRevenue ?? 0)
      ]) ?? [];
      return Math.max(1, ...values);
    });
    const inventoryMax = computed(() => {
      const values = reportingOverview.value?.inventoryByCategory?.map((category) => Number(category.pledgedValue ?? 0)) ?? [];
      return Math.max(1, ...values);
    });
    const verificationModeLabels = {
      MANUAL: () => t("tenantHome.verificationModes.MANUAL"),
      PROVIDER: () => t("tenantHome.verificationModes.PROVIDER")
    };

    function getKycStatusLabel(status) {
      return t(`customerDetail.statusOptions.kyc.${status ?? "NOT_STARTED"}`);
    }

    function getAmlStatusLabel(status) {
      return t(`customerDetail.statusOptions.aml.${status ?? "NOT_REVIEWED"}`);
    }

    function getRiskLevelLabel(level) {
      return t(`customerDetail.riskLevels.${level ?? "MEDIUM"}`);
    }

    function getVerificationModeLabel(mode) {
      return verificationModeLabels[mode]?.() ?? mode ?? t("common.notAvailable");
    }

    function getTransactionTypeLabel(type) {
      const key = `tenantHome.transactionTypes.${type}`;
      const translated = t(key);
      return translated === key ? (type ?? t("common.notAvailable")) : translated;
    }

    async function enrichCustomerCompliance(customer) {
      try {
        const amlStatus = amlFeatureEnabled.value
          ? await fetchAmlStatus(tenantStore.selectedTenantId, customer.id, authStore.token)
          : null;
        return amlStatus ? mergeAmlStatus(customer, amlStatus) : customer;
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

    async function loadContext() {
      if (!tenantStore.selectedTenantId) {
        isLoading.value = false;
        return;
      }

      isLoading.value = true;
      fieldErrors.value = [];
      errorMessage.value = "";

      const [guidelineResult, customerResult] = await Promise.allSettled([
          fetchValuationGuidelines(tenantStore.selectedTenantId, authStore.token),
          searchCustomers(tenantStore.selectedTenantId, "", authStore.token)
      ]);

      try {
        if (guidelineResult.status === "fulfilled") {
          guidelines.value = guidelineResult.value;
        } else {
          guidelines.value = [];
        }

        if (customerResult.status === "fulfilled") {
          customers.value = await Promise.all(customerResult.value.map((customer) => enrichCustomerCompliance(customer)));
        } else {
          customers.value = [];
        }

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

    async function loadReportingOverview() {
      if (!tenantStore.selectedTenantId) {
        reportingOverview.value = null;
        return;
      }

      try {
        reportingError.value = "";
        reportingOverview.value = await fetchDashboardOverview(tenantStore.selectedTenantId, authStore.token, 14);
      } catch (error) {
        reportingOverview.value = null;
        reportingError.value = error instanceof Error ? error.message : t("tenantHome.messages.reportingFailed");
      }
    }

    async function searchCustomerSuggestions(event = {}) {
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

        const customerResponse = await searchCustomers(tenantStore.selectedTenantId, query, authStore.token);
        customers.value = await Promise.all(customerResponse.map((customer) => enrichCustomerCompliance(customer)));
        customerSuggestions.value = customerOptions.value;
      } catch (error) {
        handleError(error);
      }
    }

    function handleCustomerSelection(event) {
      const option = event?.value ?? selectedCustomerOption.value;
      selectedCustomerOption.value = option && typeof option === "object" ? option : null;
      selectedCustomerId.value = option?.value ?? "";
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
          groups.get(ticketGroup).push(position);
          return groups;
        }, new Map());

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

              const quote = await fetchPawnTicketQuote(
                {
                  loanAmount,
                  termMonths: Number(terms.termMonths),
                  manualMonthlyOperatingFee: terms.manualMonthlyOperatingFee
                    ? Number(terms.manualMonthlyOperatingFee)
                    : null
                },
                authStore.token
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

    function addPosition() {
      const nextGroup = positions.value.reduce((max, position) => Math.max(max, Number(position.ticketGroup) || 0), 0) + 1;
      positions.value = [...positions.value, { ...createEmptyPosition(), ticketGroup: nextGroup }];
    }

    function removePosition(index) {
      if (positions.value.length === 1) {
        return;
      }
      positions.value = positions.value.filter((_, currentIndex) => currentIndex !== index);
    }

    function applyGuideline(index, guidelineId) {
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

      if (!canSubmitLoan.value) {
        errorMessage.value = t("tenantHome.messages.missingRequiredFields");
        return;
      }

      try {
        isSubmitting.value = true;
        let customerId = selectedCustomerId.value;

        if (useNewCustomer.value) {
          const createdCustomer = await createCustomer(tenantStore.selectedTenantId, { ...newCustomer }, authStore.token);
          const kycStatus = await updateKycStatus(
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
            },
            authStore.token
          );
          const amlStatus = amlFeatureEnabled.value
            ? await updateAmlStatus(
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
                },
                authStore.token
              )
            : null;
          customerId = createdCustomer.id;
          const mergedCustomer = mergeKycStatus(createdCustomer, kycStatus);
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

        createdLoan.value = await createLoan(tenantStore.selectedTenantId, payload, authStore.token);
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

    async function updateSelectedCustomerKyc(verificationMode) {
      if (!tenantStore.selectedTenantId || !selectedCustomerId.value) {
        return;
      }

      try {
        isUpdatingKyc.value = true;
        fieldErrors.value = [];
        const kycStatus = await updateKycStatus(
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
              },
          authStore.token
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
      if (!tenantStore.selectedTenantId) {
        return;
      }
      if (!newCustomerKyc.documentFrontImageDataUrl && !newCustomerKyc.documentBackImageDataUrl) {
        return;
      }

      try {
        fieldErrors.value = [];
        const result = await prefillKycDocument(
          tenantStore.selectedTenantId,
          "new-customer",
          {
            documentFrontImageDataUrl: newCustomerKyc.documentFrontImageDataUrl,
            documentBackImageDataUrl: newCustomerKyc.documentBackImageDataUrl
          },
          authStore.token
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

    async function loadSelectedCustomerKyc() {
      if (!tenantStore.selectedTenantId || !selectedCustomerId.value || useNewCustomer.value) {
        return;
      }

      try {
        const [kycStatus, amlStatus] = await Promise.all([
          fetchKycStatus(tenantStore.selectedTenantId, selectedCustomerId.value, authStore.token),
          amlFeatureEnabled.value
            ? assessAmlOrigination(
                tenantStore.selectedTenantId,
                selectedCustomerId.value,
                { loanAmount: Number(totalLoanValue.value || 0) },
                authStore.token
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

    async function updateNewCustomerDocument(side, event) {
      const [file] = event?.target?.files ?? [];
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

    async function updatePowerOfAttorneyDocument(event) {
      const [file] = event?.target?.files ?? [];
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

    async function openPawnTicketDocument(ticketNumber, printMode = false) {
      if (!ticketNumber) {
        return;
      }

      try {
        isDownloadingTicket.value = true;
        const blob = await fetchPawnTicketDocument(ticketNumber, authStore.token);
        const documentUrl = URL.createObjectURL(blob);
        const popup = window.open(documentUrl, "_blank", "noopener,noreferrer");

        if (printMode && popup) {
          popup.addEventListener("load", () => {
            popup.focus();
            popup.print();
          }, { once: true });
        }
      } catch (error) {
        handleError(error);
      } finally {
        isDownloadingTicket.value = false;
      }
    }

    async function openPawnTicketLabels(ticketNumber, printMode = false) {
      if (!ticketNumber) {
        return;
      }

      try {
        isDownloadingTicket.value = true;
        const blob = await fetchPawnTicketLabels(ticketNumber, authStore.token);
        const documentUrl = URL.createObjectURL(blob);
        const popup = window.open(documentUrl, "_blank", "noopener,noreferrer");

        if (printMode && popup) {
          popup.addEventListener("load", () => {
            popup.focus();
            popup.print();
          }, { once: true });
        }
      } catch (error) {
        handleError(error);
      } finally {
        isDownloadingTicket.value = false;
      }
    }

    function handleError(error) {
      errorMessage.value = error instanceof Error ? error.message : t("common.requestFailed");
      fieldErrors.value = Array.isArray(error?.fieldErrors) ? error.fieldErrors : [];
    }

    onMounted(loadContext);
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
      createdLoan,
      customerQuery,
      customerSuggestions,
      customers,
      customerOptions,
      handleCustomerSelection,
      amlFeatureEnabled,
      errorMessage,
      fieldErrors,
      guidelines,
      guidelineOptions,
      documentTypeOptions,
      amlStatusOptions,
      amlRiskLevelOptions,
      isLoading,
      isSubmitting,
      isDownloadingTicket,
      isUpdatingKyc,
      isUpdatingAml,
      issuedTicketRef,
      loanQuotes,
      newCustomer,
      newCustomerAml,
      newCustomerKyc,
      openSelectedCustomerDetails,
      openPawnTicketDocument,
      openPawnTicketLabels,
      positions,
      pledgePresentation,
      reportingError,
      reportingOverview,
      removePosition,
      searchCustomerSuggestions,
      selectedCustomer,
      selectedCustomerId,
      selectedCustomerOption,
      selectedTenant,
      submitLoan,
      successMessage,
      t,
      approveSelectedCustomerKyc,
      startProviderVerification,
      providerVerificationAvailable,
      documentOcrAvailable,
      financeTrendMax,
      formatCurrency,
      getAmlStatusLabel,
      getKycStatusLabel,
      getRiskLevelLabel,
      getTransactionTypeLabel,
      getVerificationModeLabel,
      inventoryMax,
      canSubmitLoan,
      terms,
      totalLoanValue,
      prefillNewCustomerDocumentData,
      updateNewCustomerDocument,
      updatePowerOfAttorneyDocument,
      useNewCustomer
    };
  },
  components: {
    FormFeedback
  },
  template
});
