import { computed, reactive, ref, type Ref } from "vue";
import { useRequestFeedback } from "../../../../../shared/ui/composables/use-request-feedback";
import { createHttpCustomerAdapter } from "../../../infrastructure/adapters/http-customer.adapter";
import { mapKycDomainToUpdatePayload } from "../../../infrastructure/mappers/customer-api.mapper";
import { readFileAsDataUrl, firstSelectedFile } from "../../../../../shared/kernel/files/data-url";
import { KycStatus, KycVerificationMode } from "../../../domain/model/customer-enums";

export function useCustomerKycForm({
  tenantId,
  customerId,
  t,
  ocrAvailable
}: {
  tenantId: Ref<string>;
  customerId: Ref<string>;
  t: (key: string, params?: Record<string, unknown>) => string;
  ocrAvailable: Ref<boolean>;
}) {
  const adapter = createHttpCustomerAdapter();
  const { errorMessage, successMessage, fieldErrors, resetFeedback, handleError } = useRequestFeedback(t);
  const isSaving = ref(false);
  const isPrefilling = ref(false);

  const state = reactive({
    customerId: "",
    status: KycStatus.NOT_STARTED,
    verificationMode: KycVerificationMode.MANUAL,
    verifiedUntil: null as string | null,
    documentType: null as string | null,
    documentNumber: null as string | null,
    documentValidUntil: null as string | null,
    documentFrontImageDataUrl: "",
    documentBackImageDataUrl: "",
    decisionNote: null as string | null,
    providerName: null as string | null,
    providerReference: null as string | null,
    providerStatus: null as string | null,
    providerVerificationAvailable: false
  });

  const canPrefill = computed(() => Boolean(state.documentFrontImageDataUrl));

  const statusOptions = computed(() => [
    { label: t("customerDetail.statusOptions.kyc.NOT_STARTED"), value: KycStatus.NOT_STARTED },
    { label: t("customerDetail.statusOptions.kyc.IN_PROGRESS"), value: KycStatus.IN_PROGRESS },
    { label: t("customerDetail.statusOptions.kyc.APPROVED"), value: KycStatus.APPROVED },
    { label: t("customerDetail.statusOptions.kyc.REJECTED"), value: KycStatus.REJECTED }
  ]);

  const documentTypeOptions = computed(() => [
    { label: t("customerDetail.documentTypeOptions.PERSONALAUSWEIS"), value: "PERSONALAUSWEIS" },
    { label: t("customerDetail.documentTypeOptions.REISEPASS"), value: "REISEPASS" },
    { label: t("customerDetail.documentTypeOptions.AUFENTHALTSTITEL"), value: "AUFENTHALTSTITEL" }
  ]);

  async function save() {
    if (!tenantId.value || !customerId.value) return;

    if (!state.documentFrontImageDataUrl || !state.documentBackImageDataUrl) {
      errorMessage.value = t("customerDetail.messages.documentImagesRequired");
      return;
    }

    try {
      isSaving.value = true;
      resetFeedback();
      const updated = await adapter.saveKyc(
        tenantId.value,
        customerId.value,
        mapKycDomainToUpdatePayload(state, {
          documentFrontImageDataUrl: state.documentFrontImageDataUrl,
          documentBackImageDataUrl: state.documentBackImageDataUrl
        })
      );
      Object.assign(state, {
        ...state,
        customerId: updated.customerId,
        status: updated.status,
        verificationMode: updated.verificationMode,
        verifiedUntil: updated.verifiedUntil,
        documentType: updated.documentType,
        documentNumber: updated.documentNumber,
        documentValidUntil: updated.documentValidUntil,
        decisionNote: updated.decisionNote,
        providerName: updated.providerName,
        providerReference: updated.providerReference,
        providerStatus: updated.providerStatus,
        providerVerificationAvailable: updated.providerVerificationAvailable
      });
      successMessage.value = t("customerDetail.messages.kycSaved");
    } catch (error) {
      handleError(error);
    } finally {
      isSaving.value = false;
    }
  }

  async function prefill() {
    if (!tenantId.value || !customerId.value || !state.documentFrontImageDataUrl) return;

    try {
      isPrefilling.value = true;
      resetFeedback();
      const result = await adapter.prefillKycDocument(
        tenantId.value,
        customerId.value,
        {
          documentFrontImageDataUrl: state.documentFrontImageDataUrl,
          documentBackImageDataUrl: state.documentBackImageDataUrl
        }
      );
      if (!result.available || !result.matched) {
        errorMessage.value = t("customerDetail.messages.ocrUnavailable");
        return;
      }
      state.documentType = result.documentType ?? state.documentType;
      state.documentNumber = result.documentNumber ?? state.documentNumber;
      state.documentValidUntil = result.documentValidUntil ?? state.documentValidUntil;
    } catch (error) {
      handleError(error);
    } finally {
      isPrefilling.value = false;
    }
  }

  async function updateDocument(side: "documentFrontImageDataUrl" | "documentBackImageDataUrl", event: unknown) {
    const file = firstSelectedFile(event);
    if (!file) {
      state[side] = "";
      return;
    }
    try {
      state[side] = await readFileAsDataUrl(file, t("common.fileReadFailed"));
      if (ocrAvailable.value && state.documentFrontImageDataUrl) {
        await prefill();
      }
    } catch (error) {
      handleError(error);
    }
  }

  function clearDocument(side: "documentFrontImageDataUrl" | "documentBackImageDataUrl") {
    state[side] = "";
  }

  return {
    state,
    isSaving,
    isPrefilling,
    errorMessage,
    successMessage,
    fieldErrors,
    statusOptions,
    documentTypeOptions,
    canPrefill,
    save,
    prefill,
    updateDocument,
    clearDocument
  };
}
