import { computed, reactive, ref } from "vue";
import { getRequestErrorMessage } from "../../../../shared/kernel/errors/request-error";
import { createHttpCashdeskAdapter } from "../../infrastructure/adapters/http-cashdesk.adapter";

type PawnTicketSummary = {
  ticketNumber: string;
  totalLoanValue?: number | string | null;
};

type CashTransaction = {
  type: string;
  bookingDate?: string;
  amount?: number | string;
};

type Settlement = {
  totalAmount?: number | string;
  repaymentAmount?: number | string;
  interestAmount?: number | string;
  feeAmount?: number | string;
};

type FieldError = {
  field: string;
  message: string;
};

type ErrorWithFieldErrors = {
  fieldErrors?: unknown;
};

function parseOptionalAmount(value: string) {
  return value === "" ? null : Number(value);
}

export function useCashdeskPage({
  t,
  tenantStore
}: {
  t: (key: string, params?: Record<string, unknown>) => string;
  tenantStore: { selectedTenantId: string };
}) {
  const adapter = createHttpCashdeskAdapter();
  const tickets = ref<PawnTicketSummary[]>([]);
  const selectedTicketNumber = ref("");
  const activeAction = ref("redeem");
  const settlement = ref<Settlement | null>(null);
  const transactions = ref<CashTransaction[]>([]);
  const isLoading = ref(true);
  const isSubmitting = ref(false);
  const errorMessage = ref("");
  const fieldErrors = ref<FieldError[]>([]);
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

  function resetFeedback() {
    errorMessage.value = "";
    fieldErrors.value = [];
  }

  function selectedOutstandingLoanAmount() {
    return Number(selectedTicket.value?.totalLoanValue);
  }

  function manualMonthlyOperatingFee() {
    return parseOptionalAmount(form.manualMonthlyOperatingFee);
  }

  function requireSelectedTicket(): PawnTicketSummary {
    if (!selectedTicket.value) {
      throw new Error("A pawn ticket must be selected.");
    }

    return selectedTicket.value;
  }

  function buildRedemptionPayload() {
    return {
      outstandingLoanAmount: selectedOutstandingLoanAmount(),
      remainingTermMonths: Number(form.remainingTermMonths),
      manualMonthlyOperatingFee: manualMonthlyOperatingFee()
    };
  }

  function buildExtensionPayload() {
    return {
      outstandingLoanAmount: selectedOutstandingLoanAmount(),
      extensionMonths: Number(form.extensionMonths),
      manualMonthlyOperatingFee: manualMonthlyOperatingFee()
    };
  }

  function buildPartialRepaymentPayload() {
    return {
      outstandingLoanAmount: selectedOutstandingLoanAmount(),
      repaymentAmount: Number(form.repaymentAmount),
      remainingTermMonths: Number(form.remainingTermMonths),
      manualMonthlyOperatingFee: manualMonthlyOperatingFee()
    };
  }

  function transactionType() {
    if (activeAction.value === "extend") {
      return "EXTEND";
    }
    if (activeAction.value === "partial") {
      return "PARTIAL_REPAYMENT";
    }
    return "REDEEM";
  }

  function buildCashTransactionPayload() {
    const currentTicket = requireSelectedTicket();

    return {
      tenantId: tenantStore.selectedTenantId,
      ticketNumber: currentTicket.ticketNumber,
      type: transactionType(),
      outstandingLoanAmount: selectedOutstandingLoanAmount(),
      extensionMonths: activeAction.value === "extend" ? Number(form.extensionMonths) : null,
      repaymentAmount: activeAction.value === "partial" ? Number(form.repaymentAmount) : null,
      remainingTermMonths: activeAction.value !== "extend" ? Number(form.remainingTermMonths) : null,
      manualMonthlyOperatingFee: manualMonthlyOperatingFee(),
      note: t("cashdesk.transactionNote")
    };
  }

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
        adapter.fetchPawnTickets(tenantStore.selectedTenantId),
        adapter.fetchCashTransactions(tenantStore.selectedTenantId)
      ]);
      tickets.value = ticketResponse;
      transactions.value = transactionResponse;
      if (!selectedTicketNumber.value && tickets.value.length > 0) {
        selectedTicketNumber.value = tickets.value[0].ticketNumber;
      }
    } catch (error: unknown) {
      handleError(error);
    } finally {
      isLoading.value = false;
    }
  }

  async function calculate() {
    if (!selectedTicket.value) {
      return;
    }

    resetFeedback();
    settlement.value = null;

    try {
      isSubmitting.value = true;

      if (activeAction.value === "extend") {
        settlement.value = await adapter.extendPawnTicket(buildExtensionPayload());
        return;
      }

      if (activeAction.value === "partial") {
        settlement.value = await adapter.calculatePartialRepayment(buildPartialRepaymentPayload());
        return;
      }

      settlement.value = await adapter.redeemPawnTicket(buildRedemptionPayload());
    } catch (error: unknown) {
      handleError(error);
    } finally {
      isSubmitting.value = false;
    }
  }

  async function execute() {
    if (!selectedTicket.value || !settlement.value) {
      return;
    }

    resetFeedback();

    try {
      isSubmitting.value = true;
      await adapter.executeCashTransaction(buildCashTransactionPayload());
      settlement.value = null;
      await loadData();
    } catch (error: unknown) {
      handleError(error);
    } finally {
      isSubmitting.value = false;
    }
  }

  function handleError(error: unknown) {
    const errorWithFieldErrors = error as ErrorWithFieldErrors;
    errorMessage.value = getRequestErrorMessage(error, t("common.requestFailed"));
    fieldErrors.value = Array.isArray(errorWithFieldErrors?.fieldErrors)
      ? errorWithFieldErrors.fieldErrors.filter(isFieldError)
      : [];
  }

  function isFieldError(value: unknown): value is FieldError {
    return typeof value === "object"
      && value !== null
      && typeof (value as FieldError).field === "string"
      && typeof (value as FieldError).message === "string";
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
