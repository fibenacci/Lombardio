import { computed, ref } from "vue";
import type { LoanModel } from "../../domain/model/loan";
import { getRequestErrorMessage } from "../../../../shared/kernel/errors/request-error";
import { createLoadLoansService } from "../../application/services/load-loans.service";
import { createHttpLoansAdapter } from "../../infrastructure/adapters/http-loans.adapter";

export function useLoansPage({
  t,
  tenantStore
}: {
  t: (key: string, params?: Record<string, unknown>) => string;
  tenantStore: { selectedTenantId: string };
}) {
  const adapter = createHttpLoansAdapter();
  const loadLoans = createLoadLoansService(adapter);
  const loans = ref<LoanModel[]>([]);
  const isLoading = ref(true);
  const errorMessage = ref("");
  const query = ref("");

  const filteredLoans = computed(() => {
    const normalizedQuery = query.value.trim().toLowerCase();
    return loans.value.filter((loan) =>
      !normalizedQuery
      || loan.customer.customerNumber.toLowerCase().includes(normalizedQuery)
      || loan.customer.displayName.toLowerCase().includes(normalizedQuery)
      || loan.pawnTickets.some((ticket: LoanModel["pawnTickets"][number]) =>
        ticket.ticketNumber.toLowerCase().includes(normalizedQuery)
      )
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
      loans.value = await loadLoans(tenantStore.selectedTenantId);
    } catch (error) {
      errorMessage.value = getRequestErrorMessage(error, t("common.requestFailed"));
    } finally {
      isLoading.value = false;
    }
  }

  return {
    errorMessage,
    filteredLoans,
    isLoading,
    loadData,
    query
  };
}
