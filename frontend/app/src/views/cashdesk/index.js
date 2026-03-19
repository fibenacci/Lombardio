import { computed, defineComponent, onMounted, reactive, ref } from "vue";
import { authStore } from "../../stores/auth";
import { tenantStore } from "../../stores/tenant";
import {
  calculatePartialRepayment,
  executeCashTransaction,
  extendPawnTicket,
  fetchCashTransactions,
  fetchPawnTickets,
  redeemPawnTicket
} from "../../services/api/pawnTicket";
import { useI18n } from "../../i18n";
import FormFeedback from "../../components/form-feedback";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "CashdeskView",
  components: {
    FormFeedback
  },
  setup() {
    const { t } = useI18n();
    const tickets = ref([]);
    const selectedTicketNumber = ref("");
    const activeAction = ref("redeem");
    const settlement = ref(null);
    const transactions = ref([]);
    const isLoading = ref(true);
    const isSubmitting = ref(false);
    const errorMessage = ref("");
    const fieldErrors = ref([]);
    const form = reactive({
      extensionMonths: 1,
      repaymentAmount: "",
      remainingTermMonths: 3,
      manualMonthlyOperatingFee: ""
    });

    const selectedTicket = computed(() =>
      tickets.value.find((ticket) => ticket.ticketNumber === selectedTicketNumber.value) ?? null
    );

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
          fetchPawnTickets(tenantStore.selectedTenantId, authStore.token),
          fetchCashTransactions(tenantStore.selectedTenantId, authStore.token)
        ]);
        tickets.value = ticketResponse;
        transactions.value = transactionResponse;
        if (!selectedTicketNumber.value && tickets.value.length > 0) {
          selectedTicketNumber.value = tickets.value[0].ticketNumber;
        }
      } catch (error) {
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
        const manualMonthlyOperatingFee = form.manualMonthlyOperatingFee === ""
          ? null
          : Number(form.manualMonthlyOperatingFee);

        if (activeAction.value === "extend") {
          settlement.value = await extendPawnTicket(
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
          settlement.value = await calculatePartialRepayment(
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

        settlement.value = await redeemPawnTicket(
          {
            outstandingLoanAmount,
            remainingTermMonths: Number(form.remainingTermMonths),
            manualMonthlyOperatingFee
          },
          authStore.token
        );
      } catch (error) {
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
        const payload = {
          tenantId: tenantStore.selectedTenantId,
          ticketNumber: selectedTicket.value.ticketNumber,
          type: activeAction.value === "extend"
            ? "EXTEND"
            : activeAction.value === "partial"
              ? "PARTIAL_REPAYMENT"
              : "REDEEM",
          outstandingLoanAmount: Number(selectedTicket.value.totalLoanValue),
          extensionMonths: activeAction.value === "extend" ? Number(form.extensionMonths) : null,
          repaymentAmount: activeAction.value === "partial" ? Number(form.repaymentAmount) : null,
          remainingTermMonths: activeAction.value !== "extend" ? Number(form.remainingTermMonths) : null,
          manualMonthlyOperatingFee: form.manualMonthlyOperatingFee === ""
            ? null
            : Number(form.manualMonthlyOperatingFee),
          note: t("cashdesk.transactionNote")
        };

        await executeCashTransaction(payload, authStore.token);
        settlement.value = null;
        await loadData();
      } catch (error) {
        handleError(error);
      } finally {
        isSubmitting.value = false;
      }
    }

    function handleError(error) {
      errorMessage.value = error instanceof Error ? error.message : t("common.requestFailed");
      fieldErrors.value = Array.isArray(error?.fieldErrors) ? error.fieldErrors : [];
    }

    onMounted(loadData);

    return {
      activeAction,
      calculate,
      execute,
      errorMessage,
      fieldErrors,
      form,
      isLoading,
      isSubmitting,
      selectedTicket,
      selectedTicketNumber,
      settlement,
      t,
      tenantStore,
      tickets,
      transactions
    };
  },
  template
});
