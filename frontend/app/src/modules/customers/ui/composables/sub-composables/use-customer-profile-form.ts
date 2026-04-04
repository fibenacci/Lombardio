import { reactive, ref, type Ref } from "vue";
import { useRequestFeedback } from "../../../../../shared/ui/composables/use-request-feedback";
import { createHttpCustomerAdapter } from "../../../infrastructure/adapters/http-customer.adapter";
import {
  mapCustomerDomainToUpdatePayload,
  mapCustomerDtoToDomain
} from "../../../infrastructure/mappers/customer-api.mapper";

export function useCustomerProfileForm({
  tenantId,
  customerId,
  t
}: {
  tenantId: Ref<string>;
  customerId: Ref<string>;
  t: (key: string, params?: Record<string, unknown>) => string;
}) {
  const adapter = createHttpCustomerAdapter();
  const { errorMessage, successMessage, fieldErrors, resetFeedback, handleError } = useRequestFeedback(t);
  const isSaving = ref(false);

  const state = reactive({
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

  async function save() {
    if (!tenantId.value || !customerId.value) return;

    try {
      isSaving.value = true;
      resetFeedback();
      const updated = await adapter.saveCustomer(
        tenantId.value,
        customerId.value,
        mapCustomerDomainToUpdatePayload(state)
      );
      Object.assign(state, mapCustomerDtoToDomain(updated));
      successMessage.value = t("customerDetail.messages.customerSaved");
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
    save
  };
}
