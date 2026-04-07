import { computed, ref } from "vue";
import { normalizeDocumentImageSrc } from "../../../../shared/kernel/utils/document-data-url";
import { createHttpCustomerAdapter } from "../../infrastructure/adapters/http-customer.adapter";
import {
  mapAmlToDomain,
  mapCustomerDtoToDomain,
  mapKycToDomain,
  mapLoanDtosToDomain
} from "../../infrastructure/mappers/customer-api.mapper";
import type { CustomerLoanModel } from "../../domain/model/customer";
import { useCustomerProfileForm } from "./sub-composables/use-customer-profile-form";
import { useCustomerKycForm } from "./sub-composables/use-customer-kyc-form";
import { useCustomerAmlForm } from "./sub-composables/use-customer-aml-form";
import { useRequestFeedback } from "../../../../shared/ui/composables/use-request-feedback";

export function useCustomerDetailPage({
  route,
  t,
  tenantStore
}: {
  route: { params: Record<string, unknown> };
  t: (key: string, params?: Record<string, unknown>) => string;
  tenantStore: { selectedTenant: unknown; selectedTenantId: string; hasFeature: (key: string) => boolean };
}) {
  const customerAdapter = createHttpCustomerAdapter();
  const isLoading = ref(true);
  const { errorMessage, fieldErrors, handleError } = useRequestFeedback(t);
  
  const tenant = computed(() => tenantStore.selectedTenant);
  const customerId = computed(() => String(route.params.customerId ?? ""));
  const selectedTenantId = computed(() => tenantStore.selectedTenantId);
  const documentOcrAvailable = computed(() => tenantStore.hasFeature("kyc-document-ocr"));
  const amlFeatureEnabled = computed(() => tenantStore.hasFeature("aml-compliance"));

  const profileForm = useCustomerProfileForm({ tenantId: selectedTenantId, customerId, t });
  const kycForm = useCustomerKycForm({ tenantId: selectedTenantId, customerId, t, ocrAvailable: documentOcrAvailable });
  const amlForm = useCustomerAmlForm({ tenantId: selectedTenantId, customerId, t, amlFeatureEnabled });

  const loans = ref<CustomerLoanModel[]>([]);
  const resolveDocumentImageSrc = (value: string) => normalizeDocumentImageSrc(value);

  async function loadData() {
    if (!selectedTenantId.value || !customerId.value) {
      isLoading.value = false;
      return;
    }

    isLoading.value = true;
    try {
      const result = await customerAdapter.loadCustomerDetailData(
        selectedTenantId.value,
        customerId.value,
        amlFeatureEnabled.value
      );

      Object.assign(profileForm.state, mapCustomerDtoToDomain(result.customer));
      loans.value = mapLoanDtosToDomain(result.loans);
      Object.assign(kycForm.state, mapKycToDomain(result.kycStatus, result.kycDocuments));
      if (result.aml) {
        Object.assign(amlForm.state, mapAmlToDomain(result.aml));
      }


    } catch (error) {
      handleError(error);
    } finally {
      isLoading.value = false;
    }
  }

  return {
    // Shared state
    tenant,
    customerId,
    isLoading,
    errorMessage,
    fieldErrors,
    successMessage: "", // Template expects this, but we show local success messages in sub-forms or use global toast
    loans,
    documentOcrAvailable,
    amlFeatureEnabled,
    resolveDocumentImageSrc,
    loadData,

    // Profile form
    customer: profileForm.state,
    isSavingCustomer: profileForm.isSaving,
    saveCustomer: async () => {
      await profileForm.save();
      if (profileForm.errorMessage.value) handleError(profileForm.errorMessage.value);
    },

    // KYC form
    kyc: kycForm.state,
    isSavingKyc: kycForm.isSaving,
    isPrefillingKyc: kycForm.isPrefilling,
    saveKyc: async () => {
      await kycForm.save();
      if (kycForm.errorMessage.value) handleError(kycForm.errorMessage.value);
    },
    prefillDocument: kycForm.prefill,
    updateDocument: kycForm.updateDocument,
    clearDocument: kycForm.clearDocument,
    kycStatusOptions: kycForm.statusOptions,
    kycDocumentTypeOptions: kycForm.documentTypeOptions,
    canPrefillDocument: kycForm.canPrefill,

    // AML form
    aml: amlForm.state,
    isSavingAml: amlForm.isSaving,
    saveAml: async () => {
      await amlForm.save();
      if (amlForm.errorMessage.value) handleError(amlForm.errorMessage.value);
    },
    amlStatusOptions: amlForm.statusOptions,
    amlRiskLevelOptions: amlForm.riskLevelOptions
  };
}
