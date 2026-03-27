import { computed, defineComponent, onMounted, reactive, ref } from "vue";
import { useRoute } from "vue-router";
import { fetchCustomer, updateCustomer } from "../../services/api/customer";
import { fetchLoans } from "../../services/api/origination";
import { fetchAmlStatus, updateAmlStatus } from "../../services/api/aml";
import { fetchKycStatus, prefillKycDocument, updateKycStatus } from "../../services/api/kyc";
import { useAuthStore } from "../../stores/auth";
import { useTenantStore } from "../../stores/tenant";
import { useI18n } from "../../i18n";
import FormFeedback from "../../components/form-feedback";
import template from "./template.html?raw";
import "./styles.scss";

function readFileAsDataUrl(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(typeof reader.result === "string" ? reader.result : "");
    reader.onerror = () => reject(new Error("Datei konnte nicht gelesen werden"));
    reader.readAsDataURL(file);
  });
}

function toDateTimeLocal(value) {
  if (!value) {
    return "";
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return "";
  }
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");
  return `${year}-${month}-${day}T${hours}:${minutes}`;
}

function toInstant(value) {
  if (!value) {
    return null;
  }
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? null : date.toISOString();
}

export default defineComponent({
  name: "CustomerDetailView",
  components: {
    FormFeedback
  },
  setup() {
    const { locale, t } = useI18n();
    const route = useRoute();
    const authStore = useAuthStore();
    const tenantStore = useTenantStore();
    
    const isLoading = ref(true);
    const isSavingCustomer = ref(false);
    const isSavingKyc = ref(false);
    const isSavingAml = ref(false);
    const errorMessage = ref("");
    const fieldErrors = ref([]);
    const successMessage = ref("");
    const isPrefillingKyc = ref(false);
    const loans = ref([]);
    const customer = reactive({
      id: "",
      customerNumber: "",
      firstName: "",
      lastName: "",
      birthDate: "",
      phone: "",
      email: "",
      wantsDigitalPawnTicket: false,
      onlineAccessStatus: "NOT_REQUESTED",
      street: "",
      postalCode: "",
      city: ""
    });
    const kyc = reactive({
      status: "NOT_STARTED",
      verificationMode: "MANUAL",
      verifiedUntil: "",
      documentType: "PERSONALAUSWEIS",
      documentNumber: "",
      documentValidUntil: "",
      documentFrontImageDataUrl: "",
      documentBackImageDataUrl: "",
      portraitImageDataUrl: "",
      decisionNote: ""
    });
    const aml = reactive({
      status: "NOT_REVIEWED",
      riskLevel: "MEDIUM",
      pepFlag: false,
      sanctionsHit: false,
      unusualTransactionFlag: false,
      sourceOfFundsChecked: false,
      suspiciousActivityReported: false,
      goamlReference: "",
      decisionNote: "",
      lastScreenedAt: "",
      reviewedAt: "",
      originationAllowed: false,
      decisionReason: "",
      featureAvailable: false
    });

    const tenant = computed(() => tenantStore.selectedTenant);
    const customerId = computed(() => String(route.params.customerId ?? ""));
    const documentOcrAvailable = computed(() => tenantStore.hasFeature("kyc-document-ocr"));
    const amlFeatureEnabled = computed(() => tenantStore.hasFeature("aml-compliance"));
    const dateTimeLocale = computed(() => (locale.value === "de" ? "de-DE" : "en-GB"));
    const kycStatusOptions = computed(() => [
      { label: t("customerDetail.statusOptions.kyc.NOT_STARTED"), value: "NOT_STARTED" },
      { label: t("customerDetail.statusOptions.kyc.IN_PROGRESS"), value: "IN_PROGRESS" },
      { label: t("customerDetail.statusOptions.kyc.APPROVED"), value: "APPROVED" },
      { label: t("customerDetail.statusOptions.kyc.REJECTED"), value: "REJECTED" }
    ]);
    const kycDocumentTypeOptions = computed(() => [
      { label: t("customerDetail.documentTypeOptions.PERSONALAUSWEIS"), value: "PERSONALAUSWEIS" },
      { label: t("customerDetail.documentTypeOptions.REISEPASS"), value: "REISEPASS" },
      { label: t("customerDetail.documentTypeOptions.AUFENTHALTSTITEL"), value: "AUFENTHALTSTITEL" }
    ]);
    const amlStatusOptions = computed(() => [
      { label: t("customerDetail.statusOptions.aml.NOT_REVIEWED"), value: "NOT_REVIEWED" },
      { label: t("customerDetail.statusOptions.aml.CLEAR"), value: "CLEAR" },
      { label: t("customerDetail.statusOptions.aml.REVIEW_REQUIRED"), value: "REVIEW_REQUIRED" },
      { label: t("customerDetail.statusOptions.aml.BLOCKED"), value: "BLOCKED" },
      { label: t("customerDetail.statusOptions.aml.REPORTED"), value: "REPORTED" }
    ]);
    const amlRiskLevelOptions = computed(() => [
      { label: t("customerDetail.riskLevels.LOW"), value: "LOW" },
      { label: t("customerDetail.riskLevels.MEDIUM"), value: "MEDIUM" },
      { label: t("customerDetail.riskLevels.HIGH"), value: "HIGH" }
    ]);

    async function loadData() {
      if (!tenantStore.selectedTenantId || !customerId.value) {
        isLoading.value = false;
        return;
      }

      isLoading.value = true;
      errorMessage.value = "";
      fieldErrors.value = [];

      try {
        const [customerResponse, kycResponse] = await Promise.all([
          fetchCustomer(tenantStore.selectedTenantId, customerId.value, authStore.token),
          fetchKycStatus(tenantStore.selectedTenantId, customerId.value, authStore.token)
        ]);
        const loanResponse = await fetchLoans(tenantStore.selectedTenantId, authStore.token, customerId.value);
        const amlResponse = amlFeatureEnabled.value
          ? await fetchAmlStatus(tenantStore.selectedTenantId, customerId.value, authStore.token)
          : null;

        Object.assign(customer, customerResponse);
        loans.value = loanResponse;
        Object.assign(kyc, {
          status: kycResponse.status,
          verificationMode: kycResponse.verificationMode ?? "MANUAL",
          verifiedUntil: kycResponse.verifiedUntil ?? "",
          documentType: kycResponse.documentType ?? "PERSONALAUSWEIS",
          documentNumber: kycResponse.documentNumber ?? "",
          documentValidUntil: kycResponse.documentValidUntil ?? "",
          documentFrontImageDataUrl: kycResponse.documentFrontImageDataUrl ?? "",
          documentBackImageDataUrl: kycResponse.documentBackImageDataUrl ?? "",
          decisionNote: kycResponse.decisionNote ?? ""
        });
        Object.assign(aml, {
          status: amlResponse?.status ?? "NOT_REVIEWED",
          riskLevel: amlResponse?.riskLevel ?? "MEDIUM",
          pepFlag: amlResponse?.pepFlag ?? false,
          sanctionsHit: amlResponse?.sanctionsHit ?? false,
          unusualTransactionFlag: amlResponse?.unusualTransactionFlag ?? false,
          sourceOfFundsChecked: amlResponse?.sourceOfFundsChecked ?? false,
          suspiciousActivityReported: amlResponse?.suspiciousActivityReported ?? false,
          goamlReference: amlResponse?.goamlReference ?? "",
          decisionNote: amlResponse?.decisionNote ?? "",
          lastScreenedAt: toDateTimeLocal(amlResponse?.lastScreenedAt),
          reviewedAt: toDateTimeLocal(amlResponse?.reviewedAt),
          originationAllowed: amlResponse?.originationAllowed ?? false,
          decisionReason: amlResponse?.decisionReason ?? "",
          featureAvailable: amlResponse?.featureAvailable ?? amlFeatureEnabled.value
        });
      } catch (error) {
        handleError(error);
      } finally {
        isLoading.value = false;
      }
    }

    async function saveCustomer() {
      if (!tenantStore.selectedTenantId || !customerId.value) {
        return;
      }

      try {
        isSavingCustomer.value = true;
        errorMessage.value = "";
        fieldErrors.value = [];
        successMessage.value = "";
        const updated = await updateCustomer(
          tenantStore.selectedTenantId,
          customerId.value,
          {
            customerNumber: customer.customerNumber,
            firstName: customer.firstName,
            lastName: customer.lastName,
            birthDate: customer.birthDate,
            phone: customer.phone,
            email: customer.email,
            wantsDigitalPawnTicket: customer.wantsDigitalPawnTicket,
            street: customer.street,
            postalCode: customer.postalCode,
            city: customer.city
          },
          authStore.token
        );
        Object.assign(customer, updated);
        successMessage.value = t("customerDetail.messages.customerSaved");
      } catch (error) {
        handleError(error);
      } finally {
        isSavingCustomer.value = false;
      }
    }

    async function saveKyc() {
      if (!tenantStore.selectedTenantId || !customerId.value) {
        return;
      }

      try {
        isSavingKyc.value = true;
        errorMessage.value = "";
        fieldErrors.value = [];
        successMessage.value = "";
        const updated = await updateKycStatus(
          tenantStore.selectedTenantId,
          customerId.value,
          {
            status: kyc.status,
            verificationMode: "MANUAL",
            verifiedUntil: kyc.verifiedUntil || kyc.documentValidUntil,
            documentType: kyc.documentType,
            documentNumber: kyc.documentNumber,
            documentValidUntil: kyc.documentValidUntil,
            documentFrontImageDataUrl: kyc.documentFrontImageDataUrl,
            documentBackImageDataUrl: kyc.documentBackImageDataUrl,
            decisionNote: kyc.decisionNote,
            providerName: null,
            providerReference: null,
            providerStatus: null
          },
          authStore.token
        );
        Object.assign(kyc, {
          status: updated.status,
          verificationMode: updated.verificationMode ?? "MANUAL",
          verifiedUntil: updated.verifiedUntil ?? "",
          documentType: updated.documentType ?? "PERSONALAUSWEIS",
          documentNumber: updated.documentNumber ?? "",
          documentValidUntil: updated.documentValidUntil ?? "",
          documentFrontImageDataUrl: updated.documentFrontImageDataUrl ?? "",
          documentBackImageDataUrl: updated.documentBackImageDataUrl ?? "",
          decisionNote: updated.decisionNote ?? ""
        });
        successMessage.value = t("customerDetail.messages.kycSaved");
      } catch (error) {
        handleError(error);
      } finally {
        isSavingKyc.value = false;
      }
    }

    async function saveAml() {
      if (!tenantStore.selectedTenantId || !customerId.value || !amlFeatureEnabled.value) {
        return;
      }

      try {
        isSavingAml.value = true;
        errorMessage.value = "";
        fieldErrors.value = [];
        successMessage.value = "";
        const updated = await updateAmlStatus(
          tenantStore.selectedTenantId,
          customerId.value,
          {
            status: aml.status,
            riskLevel: aml.riskLevel,
            pepFlag: aml.pepFlag,
            sanctionsHit: aml.sanctionsHit,
            unusualTransactionFlag: aml.unusualTransactionFlag,
            sourceOfFundsChecked: aml.sourceOfFundsChecked,
            suspiciousActivityReported: aml.suspiciousActivityReported,
            goamlReference: aml.goamlReference || null,
            decisionNote: aml.decisionNote || null,
            lastScreenedAt: toInstant(aml.lastScreenedAt),
            reviewedAt: toInstant(aml.reviewedAt)
          },
          authStore.token
        );
        Object.assign(aml, {
          status: updated.status,
          riskLevel: updated.riskLevel,
          pepFlag: updated.pepFlag,
          sanctionsHit: updated.sanctionsHit,
          unusualTransactionFlag: updated.unusualTransactionFlag,
          sourceOfFundsChecked: updated.sourceOfFundsChecked,
          suspiciousActivityReported: updated.suspiciousActivityReported,
          goamlReference: updated.goamlReference ?? "",
          decisionNote: updated.decisionNote ?? "",
          lastScreenedAt: toDateTimeLocal(updated.lastScreenedAt),
          reviewedAt: toDateTimeLocal(updated.reviewedAt),
          originationAllowed: updated.originationAllowed,
          decisionReason: updated.decisionReason ?? "",
          featureAvailable: updated.featureAvailable
        });
        successMessage.value = t("customerDetail.messages.amlSaved");
      } catch (error) {
        handleError(error);
      } finally {
        isSavingAml.value = false;
      }
    }

    async function updateDocument(side, event) {
      const [file] = event?.target?.files ?? [];
      if (!file) {
        kyc[side] = "";
        return;
      }
      try {
        kyc[side] = await readFileAsDataUrl(file);
        
        // Automatically trigger OCR if feature is available
        if (documentOcrAvailable.value) {
          // If we have at least the front side, we can try to prefill
          if (kyc.documentFrontImageDataUrl) {
            await prefillDocument();
          }
        }
      } catch (error) {
        handleError(error);
      }
    }

    async function prefillDocument() {
      if (!tenantStore.selectedTenantId || !customerId.value) {
        return;
      }
      if (!kyc.documentFrontImageDataUrl && !kyc.documentBackImageDataUrl) {
        return;
      }

      try {
        isPrefillingKyc.value = true;
        errorMessage.value = "";
        fieldErrors.value = [];
        const result = await prefillKycDocument(
          tenantStore.selectedTenantId,
          customerId.value,
          {
            documentFrontImageDataUrl: kyc.documentFrontImageDataUrl,
            documentBackImageDataUrl: kyc.documentBackImageDataUrl
          },
          authStore.token
        );
        if (!result.available || !result.matched) {
          errorMessage.value = t("customerDetail.messages.ocrUnavailable");
          return;
        }
        kyc.documentType = result.documentType ?? kyc.documentType;
        kyc.documentNumber = result.documentNumber ?? kyc.documentNumber;
        kyc.documentValidUntil = result.documentValidUntil ?? kyc.documentValidUntil;
        kyc.portraitImageDataUrl = result.portraitImageDataUrl ?? kyc.portraitImageDataUrl;
        
        // Also prefill customer master data if empty or provided
        if (result.firstName) customer.firstName = result.firstName;
        if (result.lastName) customer.lastName = result.lastName;
        if (result.birthDate) customer.birthDate = result.birthDate;
      } catch (error) {
        handleError(error);
      } finally {
        isPrefillingKyc.value = false;
      }
    }

    function handleError(error) {
      errorMessage.value = error instanceof Error ? error.message : t("common.requestFailed");
      fieldErrors.value = Array.isArray(error?.fieldErrors) ? error.fieldErrors : [];
    }

    function formatDateTime(value) {
      if (!value) {
        return t("common.notAvailable");
      }
      const date = new Date(value);
      if (Number.isNaN(date.getTime())) {
        return value;
      }
      return new Intl.DateTimeFormat(dateTimeLocale.value, {
        dateStyle: "medium",
        timeStyle: "short"
      }).format(date);
    }

    onMounted(loadData);

    return {
      customer,
      loans,
      errorMessage,
      fieldErrors,
      formatDateTime,
      isLoading,
      isPrefillingKyc,
      isSavingCustomer,
      isSavingKyc,
      kyc,
      documentOcrAvailable,
      aml,
      amlFeatureEnabled,
      amlRiskLevelOptions,
      amlStatusOptions,
      kycDocumentTypeOptions,
      kycStatusOptions,
      prefillDocument,
      saveCustomer,
      saveAml,
      saveKyc,
      successMessage,
      t,
      tenant,
      tenantStore,
      updateDocument,
      isSavingAml
    };
  },
  template
});
