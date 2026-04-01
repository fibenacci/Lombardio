import * as originationApi from "../api/origination.api";
import type { LoanModel } from "../../domain/model/loan";

export function createHttpLoansAdapter() {
  return {
    fetchLoans(tenantId: string, token: string) {
      return (originationApi.fetchLoans as (tenantId: string, token: string) => Promise<LoanModel[]>)(tenantId, token);
    }
  };
}
