import { computed, defineComponent, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { fetchCustomerPortalDocument, fetchCustomerPortalPawnTickets } from "../../services/api/customerPortal";
import { customerPortalStore } from "../../stores/customerPortal";
import { useI18n } from "../../i18n";
import template from "./template.html?raw";
import "./styles.scss";

function formatCurrency(value) {
  return new Intl.NumberFormat("de-DE", {
    style: "currency",
    currency: "EUR"
  }).format(Number(value ?? 0));
}

export default defineComponent({
  name: "CustomerPortalHomeView",
  setup() {
    const router = useRouter();
    const { t } = useI18n();
    const tickets = ref([]);
    const isLoading = ref(true);
    const isDownloading = ref("");
    const errorMessage = ref("");
    const customer = computed(() => customerPortalStore.customer);

    async function loadData() {
      isLoading.value = true;
      errorMessage.value = "";
      try {
        tickets.value = await fetchCustomerPortalPawnTickets(customerPortalStore.token);
      } catch (error) {
        errorMessage.value = error instanceof Error ? error.message : t("common.requestFailed");
      } finally {
        isLoading.value = false;
      }
    }

    async function downloadDocument(ticketNumber) {
      isDownloading.value = ticketNumber;
      try {
        const blob = await fetchCustomerPortalDocument(ticketNumber, customerPortalStore.token);
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

    onMounted(() => loadData());

    return {
      customer,
      downloadDocument,
      errorMessage,
      formatCurrency,
      isDownloading,
      isLoading,
      logout,
      tickets,
      t
    };
  },
  template
});
