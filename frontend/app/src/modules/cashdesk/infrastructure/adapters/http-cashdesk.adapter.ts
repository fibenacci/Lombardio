import * as pawnTicketApi from "../../../../modules/pawn-tickets/infrastructure/api/pawn-ticket.api";

export function createHttpCashdeskAdapter() {
  return {
    calculatePartialRepayment(payload: object, token: string) {
      return pawnTicketApi.calculatePartialRepayment(payload, token);
    },
    executeCashTransaction(payload: object, token: string) {
      return pawnTicketApi.executeCashTransaction(payload, token);
    },
    extendPawnTicket(payload: object, token: string) {
      return pawnTicketApi.extendPawnTicket(payload, token);
    },
    fetchCashTransactions(tenantId: string, token: string) {
      return pawnTicketApi.fetchCashTransactions(tenantId, token);
    },
    fetchPawnTickets(tenantId: string, token: string) {
      return pawnTicketApi.fetchPawnTickets(tenantId, token);
    },
    redeemPawnTicket(payload: object, token: string) {
      return pawnTicketApi.redeemPawnTicket(payload, token);
    }
  };
}
