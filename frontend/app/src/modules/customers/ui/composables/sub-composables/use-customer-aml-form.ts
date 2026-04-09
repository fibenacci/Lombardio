import { computed, reactive, ref, type Ref } from "vue";
import { useRequestFeedback } from "../../../../../shared/ui/composables/use-request-feedback";
import { createHttpCustomerAdapter } from "../../../infrastructure/adapters/http-customer.adapter";
import {
  mapAmlDomainToUpdatePayload,
  mapAmlToDomain
} from "../../../infrastructure/mappers/customer-api.mapper";
import { AmlRiskLevel, AmlStatus } from "../../../domain/model/customer";

export function useCustomerAmlForm({
  tenantId,
  customerId,
  t,
  amlFeatureEnabled
}: {
  tenantId: Ref<string>;
  customerId: Ref<string>;
  t: (key: string, params?: Record<string, unknown>) => string;
  amlFeatureEnabled: Ref<boolean>;
}) {
  const adapter = createHttpCustomerAdapter();
  const { errorMessage, successMessage, fieldErrors, resetFeedback, handleError } = useRequestFeedback(t);
  const isSaving = ref(false);

  const state = reactive({
    customerId: "",
    status: AmlStatus.NOT_REVIEWED,
    riskLevel: AmlRiskLevel.MEDIUM,
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

  const statusOptions = computed(() => [
    { label: t("customerDetail.statusOptions.aml.NOT_REVIEWED"), value: AmlStatus.NOT_REVIEWED },
    { label: t("customerDetail.statusOptions.aml.CLEAR"), value: AmlStatus.CLEAR },
    { label: t("customerDetail.statusOptions.aml.REVIEW_REQUIRED"), value: AmlStatus.REVIEW_REQUIRED },
    { label: t("customerDetail.statusOptions.aml.BLOCKED"), value: AmlStatus.BLOCKED },
    { label: t("customerDetail.statusOptions.aml.REPORTED"), value: AmlStatus.REPORTED }
  ]);

  const riskLevelOptions = computed(() => [
    { label: t("customerDetail.riskLevels.LOW"), value: AmlRiskLevel.LOW },
    { label: t("customerDetail.riskLevels.MEDIUM"), value: AmlRiskLevel.MEDIUM },
    { label: t("customerDetail.riskLevels.HIGH"), value: AmlRiskLevel.HIGH }
  ]);


  async function save() {
    if (!tenantId.value || !customerId.value || !amlFeatureEnabled.value) return;

    try {
      isSaving.value = true;
      resetFeedback();
      const updated = await adapter.saveAml(
        tenantId.value,
        customerId.value,
        mapAmlDomainToUpdatePayload(state)
      );
      Object.assign(state, mapAmlToDomain(updated));
      successMessage.value = t("customerDetail.messages.amlSaved");
    } catch (error) {
      handleError(error);
    } finally {
      isSaving.value = false;
    }
  }

  return {
    state,
    isSaving,
    errorMessage,
    successMessage,
    fieldErrors,
    statusOptions,
    riskLevelOptions,
    save
  };
}
