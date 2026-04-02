import { computed, reactive, ref } from "vue";
import { getRequestErrorMessage } from "../../../../shared/kernel/errors/request-error";
import { normalizeDocumentImageSrc } from "../../../../shared/kernel/utils/document-data-url";
import { createHttpCustomerAdapter } from "../../infrastructure/adapters/http-customer.adapter";
import {
  mapAmlDomainToUpdatePayload,
  mapAmlToDomain,
  mapCustomerDomainToUpdatePayload,
  mapCustomerDtoToDomain,
  mapKycDomainToUpdatePayload,
  mapKycToDomain,
  mapLoanDtosToDomain
} from "../../infrastructure/mappers/customer-api.mapper";
import type { CustomerLoanModel } from "../../domain/model/customer";

function readFileAsDataUrl(file: File, t: (key: string, params?: Record<string, unknown>) => string) {
  return new Promise<string>((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(typeof reader.result === "string" ? reader.result : "");
    reader.onerror = () => reject(new Error(t("common.fileReadFailed")));
    reader.readAsDataURL(file);
  });
}

function firstSelectedFile(event: unknown) {
  const payload = event as { files?: File[]; target?: { files?: File[] } } | undefined;
  return payload?.files?.[0] ?? payload?.target?.files?.[0] ?? null;
}

function hasRequiredManualKycDocuments(kyc: { documentFrontImageDataUrl: string; documentBackImageDataUrl: string }) {
  return Boolean(kyc.documentFrontImageDataUrl && kyc.documentBackImageDataUrl);
}

export function useCustomerDetailPage({
  authStore,
  route,
  t,
  tenantStore
}: {
  authStore: Record<string, unknown>;
  route: { params: Record<string, unknown> };
  t: (key: string, params?: Record<string, unknown>) => string;
  tenantStore: { selectedTenant: unknown; selectedTenantId: string; hasFeature: (key: string) => boolean };
}) {
  const customerAdapter = createHttpCustomerAdapter();
  const isLoading = ref(true);
  const isSavingCustomer = ref(false);
  const isSavingKyc = ref(false);
  const isSavingAml = ref(false);
  const isPrefillingKyc = ref(false);
  const errorMessage = ref("");
  const fieldErrors = ref<Array<{ field: string; message: string }>>([]);
  const successMessage = ref("");
  const loans = ref<CustomerLoanModel[]>([]);
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
  const canPrefillDocument = computed(() => Boolean(kyc.documentFrontImageDataUrl));
  const resolveDocumentImageSrc = (value: string) => normalizeDocumentImageSrc(value);

  async function loadData() {
    if (!tenantStore.selectedTenantId || !customerId.value) {
      isLoading.value = false;
      return;
    }

    isLoading.value = true;
    errorMessage.value = "";
    fieldErrors.value = [];

    try {
      const result = await customerAdapter.loadCustomerDetailData(
        tenantStore.selectedTenantId,
        customerId.value,
        amlFeatureEnabled.value
      );

      Object.assign(customer, mapCustomerDtoToDomain(result.customer));
      loans.value = mapLoanDtosToDomain(result.loans);
      Object.assign(kyc, mapKycToDomain(result.kycStatus, result.kycDocuments));
      Object.assign(aml, mapAmlToDomain(result.aml, amlFeatureEnabled.value));
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
      resetFeedback();
      const updated = await customerAdapter.saveCustomer(
        tenantStore.selectedTenantId,
        customerId.value,
        mapCustomerDomainToUpdatePayload(customer)
      );
      Object.assign(customer, mapCustomerDtoToDomain(updated));
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
      resetFeedback();
      if (!hasRequiredManualKycDocuments(kyc)) {
        errorMessage.value = t("customerDetail.messages.documentImagesRequired");
        fieldErrors.value = [
          { field: "documentFrontImageDataUrl", message: t("customerDetail.messages.documentImagesRequired") },
          { field: "documentBackImageDataUrl", message: t("customerDetail.messages.documentImagesRequired") }
        ];
        return;
      }
      const updated = await customerAdapter.saveKyc(
        tenantStore.selectedTenantId,
        customerId.value,
        mapKycDomainToUpdatePayload(kyc)
      );
      Object.assign(kyc, {
        ...kyc,
        status: updated.status,
        verificationMode: updated.verificationMode ?? "MANUAL",
        verifiedUntil: updated.verifiedUntil ?? "",
        documentType: updated.documentType ?? "PERSONALAUSWEIS",
        documentNumber: updated.documentNumber ?? "",
        documentValidUntil: updated.documentValidUntil ?? "",
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
      resetFeedback();
      const updated = await customerAdapter.saveAml(
        tenantStore.selectedTenantId,
        customerId.value,
        mapAmlDomainToUpdatePayload(aml)
      );
      Object.assign(aml, mapAmlToDomain(updated, amlFeatureEnabled.value));
      successMessage.value = t("customerDetail.messages.amlSaved");
    } catch (error) {
      handleError(error);
    } finally {
      isSavingAml.value = false;
    }
  }

  async function updateDocument(side: "documentFrontImageDataUrl" | "documentBackImageDataUrl", event: unknown) {
    const file = firstSelectedFile(event);
    if (!file) {
      kyc[side] = "";
      return;
    }
    try {
      kyc[side] = await readFileAsDataUrl(file, t);
      if (documentOcrAvailable.value && kyc.documentFrontImageDataUrl) {
        await prefillDocument();
      }
    } catch (error) {
      handleError(error);
    }
  }

  function clearDocument(side: "documentFrontImageDataUrl" | "documentBackImageDataUrl") {
    kyc[side] = "";
  }

  async function prefillDocument() {
    if (!tenantStore.selectedTenantId || !customerId.value || !kyc.documentFrontImageDataUrl) {
      return;
    }
    try {
      isPrefillingKyc.value = true;
      resetFeedback();
      const result = await customerAdapter.prefillKycDocument(
        tenantStore.selectedTenantId,
        customerId.value,
        {
          documentFrontImageDataUrl: kyc.documentFrontImageDataUrl,
          documentBackImageDataUrl: kyc.documentBackImageDataUrl
        }
      );
      if (!result.available || !result.matched) {
        errorMessage.value = t("customerDetail.messages.ocrUnavailable");
        return;
      }
      kyc.documentType = result.documentType ?? kyc.documentType;
      kyc.documentNumber = result.documentNumber ?? kyc.documentNumber;
      kyc.documentValidUntil = result.documentValidUntil ?? kyc.documentValidUntil;
      kyc.portraitImageDataUrl = result.portraitImageDataUrl ?? kyc.portraitImageDataUrl;
    } catch (error) {
      handleError(error);
    } finally {
      isPrefillingKyc.value = false;
    }
  }

  function resetFeedback() {
    errorMessage.value = "";
    fieldErrors.value = [];
    successMessage.value = "";
  }

  function handleError(error: unknown) {
    errorMessage.value = getRequestErrorMessage(error, t("common.requestFailed"));
    fieldErrors.value = Array.isArray((error as { fieldErrors?: unknown } | undefined)?.fieldErrors)
      ? ((error as { fieldErrors: Array<{ field: string; message: string }> }).fieldErrors)
      : [];
  }

  return {
    aml,
    amlFeatureEnabled,
    amlRiskLevelOptions,
    amlStatusOptions,
    canPrefillDocument,
    clearDocument,
    customer,
    customerId,
    documentOcrAvailable,
    errorMessage,
    fieldErrors,
    isLoading,
    isPrefillingKyc,
    isSavingAml,
    isSavingCustomer,
    isSavingKyc,
    kyc,
    kycDocumentTypeOptions,
    kycStatusOptions,
    loans,
    loadData,
    prefillDocument,
    resolveDocumentImageSrc,
    saveAml,
    saveCustomer,
    saveKyc,
    successMessage,
    tenant,
    updateDocument
  };
}
