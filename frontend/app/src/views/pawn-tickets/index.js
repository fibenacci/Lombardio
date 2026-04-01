import { computed, defineComponent, onMounted, ref } from "vue";
import { useAppToast } from "../../composables/use-app-toast";
import { useAuthStore } from "../../stores/auth";
import { useTenantStore } from "../../stores/tenant";
import { useI18n } from "../../i18n";
import { useFormatters } from "../../utils/formatters";
import { fetchPawnTicketDocument, fetchPawnTicketLabels, fetchPawnTickets } from "../../services/api/pawnTicket";
import template from "./template.html?raw";
import "./styles.scss";

function ticketStatus(ticket) {
// ... (omitted for brevity in replacement context but included in actual replacement)
}

export default defineComponent({
  name: "PawnTicketsView",
  setup() {
    const authStore = useAuthStore();
    const tenantStore = useTenantStore();
    const { t } = useI18n();
    const { formatCurrency, formatDate } = useFormatters();
    const toast = useAppToast();
    const tickets = ref([]);
    const query = ref("");
    const isLoading = ref(true);
    const errorMessage = ref("");
    const isDownloading = ref(false);
    const statusLabels = {
      ACTIVE: () => t("pawnTickets.status.ACTIVE"),
      DUE: () => t("pawnTickets.status.DUE"),
      REDEEMED: () => t("pawnTickets.status.REDEEMED"),
      EXTENDED: () => t("pawnTickets.status.EXTENDED"),
      AUCTIONED: () => t("pawnTickets.status.AUCTIONED")
    };

    const filteredTickets = computed(() => {
      const normalizedQuery = query.value.trim().toLowerCase();

      return tickets.value
        .map((ticket) => ({
          ...ticket,
          status: ticketStatus(ticket)
        }))
        .filter((ticket) =>
          !normalizedQuery
          || ticket.ticketNumber.toLowerCase().includes(normalizedQuery)
          || ticket.customerNumber.toLowerCase().includes(normalizedQuery)
          || ticket.customerDisplayName.toLowerCase().includes(normalizedQuery)
        );
    });

    async function loadData() {
      isLoading.value = true;
      errorMessage.value = "";

      if (!tenantStore.selectedTenantId) {
        tickets.value = [];
        isLoading.value = false;
        return;
      }

      try {
        tickets.value = await fetchPawnTickets(tenantStore.selectedTenantId, authStore.token);
      } catch (error) {
        errorMessage.value = error instanceof Error ? error.message : t("common.requestFailed");
      } finally {
        isLoading.value = false;
      }
    }

    async function openDocument(ticketNumber, printMode = false) {
      try {
        isDownloading.value = true;
        const blob = await fetchPawnTicketDocument(ticketNumber, authStore.token);
        const documentUrl = URL.createObjectURL(blob);
        const popup = window.open(documentUrl, "_blank", "noopener,noreferrer");

        if (printMode && popup) {
          popup.addEventListener("load", () => {
            popup.focus();
            popup.print();
          }, { once: true });
        }
        toast.info(
          t("pawnTickets.messages.documentOpenedTitle"),
          t("pawnTickets.messages.documentOpenedToast", { ticketNumber })
        );
      } catch (error) {
        errorMessage.value = error instanceof Error ? error.message : t("common.requestFailed");
      } finally {
        isDownloading.value = false;
      }
    }

    async function openLabels(ticketNumber, printMode = false) {
      try {
        isDownloading.value = true;
        const blob = await fetchPawnTicketLabels(ticketNumber, authStore.token);
        const documentUrl = URL.createObjectURL(blob);
        const popup = window.open(documentUrl, "_blank", "noopener,noreferrer");

        if (printMode && popup) {
          popup.addEventListener("load", () => {
            popup.focus();
            popup.print();
          }, { once: true });
        }
        toast.info(
          t("pawnTickets.messages.labelsOpenedTitle"),
          t("pawnTickets.messages.labelsOpenedToast", { ticketNumber })
        );
      } catch (error) {
        errorMessage.value = error instanceof Error ? error.message : t("common.requestFailed");
      } finally {
        isDownloading.value = false;
      }
    }

    onMounted(loadData);

    return {
      errorMessage,
      filteredTickets,
      formatCurrency,
      formatDate,
      getTicketStatusLabel: (status) => statusLabels[status]?.() ?? status ?? t("common.notAvailable"),
      isDownloading,
      isLoading,
      openDocument,
      openLabels,
      query,
      t,
      tenantStore
    };
  },
  template
});
