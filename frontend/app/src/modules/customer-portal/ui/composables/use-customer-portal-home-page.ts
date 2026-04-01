import { computed, ref } from "vue";
import { createHttpCustomerPortalAdapter } from "../../infrastructure/adapters/http-customer-portal.adapter";

export function useCustomerPortalHomePage({
  customerPortalStore,
  router,
  t
}: {
  customerPortalStore: {
    customer: unknown;
    logout: () => void;
    token: string;
  };
  router: { push: (payload: object) => Promise<unknown> | unknown };
  t: (key: string, params?: Record<string, unknown>) => string;
}) {
  const adapter = createHttpCustomerPortalAdapter();
  const tickets = ref<any[]>([]);
  const isLoading = ref(true);
  const isDownloading = ref("");
  const errorMessage = ref("");
  const customer = computed(() => customerPortalStore.customer);

  async function loadData() {
    isLoading.value = true;
    errorMessage.value = "";
    try {
      tickets.value = await adapter.fetchCustomerPortalPawnTickets(customerPortalStore.token);
    } catch (error) {
      errorMessage.value = error instanceof Error ? error.message : t("common.requestFailed");
    } finally {
      isLoading.value = false;
    }
  }

  async function downloadDocument(ticketNumber: string) {
    isDownloading.value = ticketNumber;
    try {
      const blob = await adapter.fetchCustomerPortalDocument(ticketNumber, customerPortalStore.token);
      const url = URL.createObjectURL(blob);
      window.open(url, "_blank", "noopener");
    } finally {
      isDownloading.value = "";
    }
  }

  function logout() {
    customerPortalStore.logout();
    router.push({ name: "customer-portal-login" });
  }

  return {
    customer,
    downloadDocument,
    errorMessage,
    isDownloading,
    isLoading,
    loadData,
    logout,
    tickets
  };
}
