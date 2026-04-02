import type { LoanModel } from "../../domain/model/loan";

export function createLoadLoansService(adapter: { fetchLoans: (tenantId: string) => Promise<LoanModel[]> }) {
  return function loadLoans(tenantId: string) {
    return adapter.fetchLoans(tenantId);
  };
}
