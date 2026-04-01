import { computed, reactive, ref } from "vue";
import { createHttpCashdeskAdapter } from "../../infrastructure/adapters/http-cashdesk.adapter";

export function useCashdeskPage({
  authStore,
  t,
  tenantStore
}: {
  authStore: { token: string };
  t: (key: string, params?: Record<string, unknown>) => string;
  tenantStore: { selectedTenantId: string };
}) {
  const adapter = createHttpCashdeskAdapter();
  const tickets = ref<any[]>([]);
  const selectedTicketNumber = ref("");
  const activeAction = ref("redeem");
  const settlement = ref<any | null>(null);
  const transactions = ref<any[]>([]);
  const isLoading = ref(true);
  const isSubmitting = ref(false);
  const errorMessage = ref("");
  const fieldErrors = ref<any[]>([]);
  const form = reactive({
    extensionMonths: 1,
    repaymentAmount: "",
    remainingTermMonths: 3,
    manualMonthlyOperatingFee: ""
  });

  const selectedTicket = computed(
    () => tickets.value.find((ticket) => ticket.ticketNumber === selectedTicketNumber.value) ?? null
  );
  const actionOptions = computed(() => [
    { label: t("cashdesk.actions.redeem"), value: "redeem" },
    { label: t("cashdesk.actions.extend"), value: "extend" },
    { label: t("cashdesk.actions.partial"), value: "partial" }
  ]);

  async function loadData() {
    isLoading.value = true;
    errorMessage.value = "";

    if (!tenantStore.selectedTenantId) {
      tickets.value = [];
      isLoading.value = false;
      return;
    }

    try {
      const [ticketResponse, transactionResponse] = await Promise.all([
        adapter.fetchPawnTickets(tenantStore.selectedTenantId, authStore.token),
        adapter.fetchCashTransactions(tenantStore.selectedTenantId, authStore.token)
      ]);
      tickets.value = ticketResponse;
      transactions.value = transactionResponse;
      if (!selectedTicketNumber.value && tickets.value.length > 0) {
        selectedTicketNumber.value = tickets.value[0].ticketNumber;
      }
    } catch (error: any) {
      handleError(error);
    } finally {
      isLoading.value = false;
    }
  }

  async function calculate() {
    if (!selectedTicket.value) {
      return;
    }

    errorMessage.value = "";
    fieldErrors.value = [];
    settlement.value = null;

    try {
      isSubmitting.value = true;
      const outstandingLoanAmount = Number(selectedTicket.value.totalLoanValue);
      const manualMonthlyOperatingFee =
        form.manualMonthlyOperatingFee === "" ? null : Number(form.manualMonthlyOperatingFee);

      if (activeAction.value === "extend") {
        settlement.value = await adapter.extendPawnTicket(
          {
            outstandingLoanAmount,
            extensionMonths: Number(form.extensionMonths),
            manualMonthlyOperatingFee
          },
          authStore.token
        );
        return;
      }

      if (activeAction.value === "partial") {
        settlement.value = await adapter.calculatePartialRepayment(
          {
            outstandingLoanAmount,
            repaymentAmount: Number(form.repaymentAmount),
            remainingTermMonths: Number(form.remainingTermMonths),
            manualMonthlyOperatingFee
          },
          authStore.token
        );
        return;
      }

      settlement.value = await adapter.redeemPawnTicket(
        {
          outstandingLoanAmount,
          remainingTermMonths: Number(form.remainingTermMonths),
          manualMonthlyOperatingFee
        },
        authStore.token
      );
    } catch (error: any) {
      handleError(error);
    } finally {
      isSubmitting.value = false;
    }
  }

  async function execute() {
    if (!selectedTicket.value || !settlement.value) {
      return;
    }

    errorMessage.value = "";
    fieldErrors.value = [];

    try {
      isSubmitting.value = true;
      await adapter.executeCashTransaction(
        {
          tenantId: tenantStore.selectedTenantId,
          ticketNumber: selectedTicket.value.ticketNumber,
          type:
            activeAction.value === "extend"
              ? "EXTEND"
              : activeAction.value === "partial"
                ? "PARTIAL_REPAYMENT"
                : "REDEEM",
          outstandingLoanAmount: Number(selectedTicket.value.totalLoanValue),
          extensionMonths: activeAction.value === "extend" ? Number(form.extensionMonths) : null,
          repaymentAmount: activeAction.value === "partial" ? Number(form.repaymentAmount) : null,
          remainingTermMonths: activeAction.value !== "extend" ? Number(form.remainingTermMonths) : null,
          manualMonthlyOperatingFee:
            form.manualMonthlyOperatingFee === "" ? null : Number(form.manualMonthlyOperatingFee),
          note: t("cashdesk.transactionNote")
        },
        authStore.token
      );
      settlement.value = null;
      await loadData();
    } catch (error: any) {
      handleError(error);
    } finally {
      isSubmitting.value = false;
    }
  }

  function handleError(error: any) {
    errorMessage.value = error instanceof Error ? error.message : t("common.requestFailed");
    fieldErrors.value = Array.isArray(error?.fieldErrors) ? error.fieldErrors : [];
  }

  function getTransactionTypeLabel(type: string) {
    return t(`cashdesk.transactionTypes.${type}`);
  }

  return {
    activeAction,
    actionOptions,
    calculate,
    execute,
    errorMessage,
    fieldErrors,
    form,
    getTransactionTypeLabel,
    isLoading,
    isSubmitting,
    loadData,
    selectedTicket,
    selectedTicketNumber,
    settlement,
    tickets,
    transactions
  };
}
