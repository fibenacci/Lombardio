import { computed, defineComponent, onMounted, ref } from "vue";
import { fetchLoans } from "../../services/api/origination";
import { useAuthStore } from "../../stores/auth";
import { useTenantStore } from "../../stores/tenant";
import { useI18n } from "../../i18n";
import { useFormatters } from "../../utils/formatters";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "LoansView",
  setup() {
    const authStore = useAuthStore();
    const tenantStore = useTenantStore();
    const { t } = useI18n();
    const { formatCurrency, formatDate, formatDateTime } = useFormatters();
    const loans = ref([]);
    const isLoading = ref(true);
    const errorMessage = ref("");
    const query = ref("");

    const filteredLoans = computed(() => {
      const normalizedQuery = query.value.trim().toLowerCase();
      return loans.value.filter((loan) =>
        !normalizedQuery
        || loan.customer.customerNumber.toLowerCase().includes(normalizedQuery)
        || loan.customer.displayName.toLowerCase().includes(normalizedQuery)
        || loan.pawnTickets.some((ticket) => ticket.ticketNumber.toLowerCase().includes(normalizedQuery))
      );
    });

    async function loadData() {
      isLoading.value = true;
      errorMessage.value = "";

      if (!tenantStore.selectedTenantId) {
        loans.value = [];
        isLoading.value = false;
        return;
      }

      try {
        loans.value = await fetchLoans(tenantStore.selectedTenantId, authStore.token);
      } catch (error) {
        errorMessage.value = error instanceof Error ? error.message : t("common.requestFailed");
      } finally {
        isLoading.value = false;
      }
    }

    onMounted(loadData);

    return {
      errorMessage,
      filteredLoans,
      formatCurrency,
      formatDate,
      formatDateTime,
      isLoading,
      query,
      t,
      tenantStore
    };
  },
  template
});
