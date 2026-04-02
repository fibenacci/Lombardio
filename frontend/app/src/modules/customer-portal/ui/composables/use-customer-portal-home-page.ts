import { computed, ref } from "vue";
import { getRequestErrorMessage } from "../../../../shared/kernel/errors/request-error";
import { openBlobInWindow } from "../../../../shared/kernel/utils/blob-window";
import { createHttpCustomerPortalAdapter } from "../../infrastructure/adapters/http-customer-portal.adapter";

type CustomerPortalCustomer = {
  customerNumber?: string;
  displayName?: string;
  firstName?: string;
  lastName?: string;
} | null;

type CustomerPortalPawnTicket = {
  ticketNumber: string;
  status?: string;
  dueDate?: string;
};

export function useCustomerPortalHomePage({
  customerPortalStore,
  router,
  t
}: {
  customerPortalStore: {
    customer: CustomerPortalCustomer;
    logout: () => Promise<unknown> | unknown;
  };
  router: { push: (payload: object) => Promise<unknown> | unknown };
  t: (key: string, params?: Record<string, unknown>) => string;
}) {
  const adapter = createHttpCustomerPortalAdapter();
  const tickets = ref<CustomerPortalPawnTicket[]>([]);
  const isLoading = ref(true);
  const isDownloading = ref("");
  const errorMessage = ref("");
  const customer = computed(() => customerPortalStore.customer);

  async function loadData() {
    isLoading.value = true;
    errorMessage.value = "";
    try {
      tickets.value = await adapter.fetchCustomerPortalPawnTickets();
    } catch (error) {
      errorMessage.value = getRequestErrorMessage(error, t("common.requestFailed"));
    } finally {
      isLoading.value = false;
    }
  }

  async function downloadDocument(ticketNumber: string) {
    isDownloading.value = ticketNumber;
    try {
      const blob = await adapter.fetchCustomerPortalDocument(ticketNumber);
      openBlobInWindow(blob);
    } finally {
      isDownloading.value = "";
    }
  }

  async function logout() {
    await customerPortalStore.logout();
    await router.push({ name: "customer-portal-login" });
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
