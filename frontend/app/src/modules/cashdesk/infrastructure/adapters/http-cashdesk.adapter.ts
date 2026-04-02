import * as pawnTicketApi from "../../../../modules/pawn-tickets/infrastructure/api/pawn-ticket.api";

export function createHttpCashdeskAdapter() {
  return {
    calculatePartialRepayment(payload: object) {
      return pawnTicketApi.calculatePartialRepayment(payload);
    },
    executeCashTransaction(payload: object) {
      return pawnTicketApi.executeCashTransaction(payload);
    },
    extendPawnTicket(payload: object) {
      return pawnTicketApi.extendPawnTicket(payload);
    },
    fetchCashTransactions(tenantId: string) {
      return pawnTicketApi.fetchCashTransactions(tenantId);
    },
    fetchPawnTickets(tenantId: string) {
      return pawnTicketApi.fetchPawnTickets(tenantId);
    },
    redeemPawnTicket(payload: object) {
      return pawnTicketApi.redeemPawnTicket(payload);
    }
  };
}
