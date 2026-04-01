import type { LoanModel } from "../../domain/model/loan";

export function createLoadLoansService(adapter: { fetchLoans: (tenantId: string, token: string) => Promise<LoanModel[]> }) {
  return function loadLoans(tenantId: string, token: string) {
    return adapter.fetchLoans(tenantId, token);
  };
}
