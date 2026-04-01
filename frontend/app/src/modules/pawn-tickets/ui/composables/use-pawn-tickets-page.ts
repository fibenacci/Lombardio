import { computed, ref } from "vue";
import { getRequestErrorMessage } from "../../../../shared/kernel/errors/request-error";
import { createLoadPawnTicketsService } from "../../application/services/load-pawn-tickets.service";
import type { PawnTicketModel } from "../../domain/model/pawn-ticket";
import { createHttpPawnTicketsAdapter } from "../../infrastructure/adapters/http-pawn-tickets.adapter";

function resolvePawnTicketStatus(ticket: Record<string, any>, now = new Date()) {
  if (typeof ticket.status === "string" && ticket.status) {
    return ticket.status;
  }

  if (ticket.redeemedAt || ticket.settledAt) {
    return "REDEEMED";
  }

  if (ticket.auctionedAt) {
    return "AUCTIONED";
  }

  if (ticket.extendedAt) {
    return "EXTENDED";
  }

  const dueDate = ticket.dueDate ? new Date(ticket.dueDate) : null;
  const earliestAuctionDate = ticket.earliestAuctionDate ? new Date(ticket.earliestAuctionDate) : null;

  if (earliestAuctionDate && !Number.isNaN(earliestAuctionDate.valueOf()) && earliestAuctionDate <= now) {
    return "AUCTIONED";
  }

  if (dueDate && !Number.isNaN(dueDate.valueOf()) && dueDate < now) {
    return "DUE";
  }

  return "ACTIVE";
}

export function usePawnTicketsPage({
  authStore,
  t,
  tenantStore,
  toast
}: {
  authStore: { token: string };
  t: (key: string, params?: Record<string, unknown>) => string;
  tenantStore: { selectedTenantId: string };
  toast: { info: (title: string, message: string) => void };
}) {
  const adapter = createHttpPawnTicketsAdapter();
  const loadPawnTickets = createLoadPawnTicketsService(adapter);
  const tickets = ref<PawnTicketModel[]>([]);
  const query = ref("");
  const isLoading = ref(true);
  const errorMessage = ref("");
  const isDownloading = ref(false);

  const filteredTickets = computed(() => {
    const normalizedQuery = query.value.trim().toLowerCase();

    return tickets.value
      .map((ticket): PawnTicketModel & { status: string } => ({
        ...ticket,
        status: resolvePawnTicketStatus(ticket)
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
      tickets.value = await loadPawnTickets(tenantStore.selectedTenantId, authStore.token);
    } catch (error) {
      errorMessage.value = getRequestErrorMessage(error, t("common.requestFailed"));
    } finally {
      isLoading.value = false;
    }
  }

  async function openBlobDocument(
    ticketNumber: string,
    printMode: boolean,
    fetchBlob: (ticketNumber: string, token: string) => Promise<Blob>,
    titleKey: string,
    messageKey: string
  ) {
    try {
      isDownloading.value = true;
      const blob = await fetchBlob(ticketNumber, authStore.token);
      const documentUrl = URL.createObjectURL(blob);
      const popup = window.open(documentUrl, "_blank", "noopener,noreferrer");

      if (printMode && popup) {
        popup.addEventListener(
          "load",
          () => {
            popup.focus();
            popup.print();
          },
          { once: true }
        );
      }

      toast.info(t(titleKey), t(messageKey, { ticketNumber }));
    } catch (error) {
      errorMessage.value = getRequestErrorMessage(error, t("common.requestFailed"));
    } finally {
      isDownloading.value = false;
    }
  }

  function openDocument(ticketNumber: string, printMode = false) {
    return openBlobDocument(
      ticketNumber,
      printMode,
      adapter.fetchPawnTicketDocument,
      "pawnTickets.messages.documentOpenedTitle",
      "pawnTickets.messages.documentOpenedToast"
    );
  }

  function openLabels(ticketNumber: string, printMode = false) {
    return openBlobDocument(
      ticketNumber,
      printMode,
      adapter.fetchPawnTicketLabels,
      "pawnTickets.messages.labelsOpenedTitle",
      "pawnTickets.messages.labelsOpenedToast"
    );
  }

  return {
    errorMessage,
    filteredTickets,
    isDownloading,
    isLoading,
    loadData,
    openDocument,
    openLabels,
    query
  };
}
