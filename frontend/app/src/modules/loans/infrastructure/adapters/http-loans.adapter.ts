import * as originationApi from "../api/origination.api";
import type { LoanModel } from "../../domain/model/loan";

export function createHttpLoansAdapter() {
  return {
    fetchLoans(tenantId: string) {
      return (originationApi.fetchLoans as (tenantId: string) => Promise<LoanModel[]>)(tenantId);
    }
  };
}
