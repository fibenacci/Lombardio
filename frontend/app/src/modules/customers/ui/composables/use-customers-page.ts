import { ref } from "vue";
import { getRequestErrorMessage } from "../../../../shared/kernel/errors/request-error";
import { createLoadCustomerListService } from "../../application/services/load-customer-list.service";
import { createHttpCustomerAdapter } from "../../infrastructure/adapters/http-customer.adapter";

export function useCustomersPage({
  router,
  t,
  tenantStore
}: {
  router: { push: (payload: object) => unknown };
  t: (key: string, params?: Record<string, unknown>) => string;
  tenantStore: { selectedTenantId: string };
}) {
  const adapter = createHttpCustomerAdapter();
  const loadCustomerList = createLoadCustomerListService(adapter);
  const customers = ref<Array<Record<string, unknown>>>([]);
  const query = ref("");
  const isLoading = ref(true);
  const errorMessage = ref("");

  async function loadData(searchQuery = "") {
    isLoading.value = true;
    errorMessage.value = "";

    if (!tenantStore.selectedTenantId) {
      customers.value = [];
      isLoading.value = false;
      return;
    }

    try {
      customers.value = await loadCustomerList(
        {
          query: searchQuery,
          tenantId: tenantStore.selectedTenantId
        },
        t
      );
    } catch (error) {
      errorMessage.value = getRequestErrorMessage(error, t("common.requestFailed"));
    } finally {
      isLoading.value = false;
    }
  }

  async function runSearch() {
    await loadData(query.value);
  }

  function openCustomer(customerId: string) {
    if (!customerId) {
      return;
    }

    router.push({ name: "tenant-customer-detail", params: { customerId } });
  }

  return {
    customers,
    errorMessage,
    isLoading,
    loadData,
    openCustomer,
    query,
    runSearch
  };
}
