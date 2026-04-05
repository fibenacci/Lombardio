import * as pawnTicketApi from "../../../../modules/pawn-tickets/infrastructure/api/pawn-ticket.api";

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

export function createHttpCashdeskAdapter() {
  return {
    calculatePartialRepayment(payload: object): Promise<Settlement> {
      return pawnTicketApi.calculatePartialRepayment(payload) as Promise<Settlement>;
    },
    executeCashTransaction(payload: object) {
      return pawnTicketApi.executeCashTransaction(payload);
    },
    extendPawnTicket(payload: object): Promise<Settlement> {
      return pawnTicketApi.extendPawnTicket(payload) as Promise<Settlement>;
    },
    fetchCashTransactions(tenantId: string): Promise<CashTransaction[]> {
      return pawnTicketApi.fetchCashTransactions(tenantId) as Promise<CashTransaction[]>;
    },
    fetchPawnTickets(tenantId: string): Promise<PawnTicketSummary[]> {
      return pawnTicketApi.fetchPawnTickets(tenantId) as Promise<PawnTicketSummary[]>;
    },
    redeemPawnTicket(payload: object): Promise<Settlement> {
      return pawnTicketApi.redeemPawnTicket(payload) as Promise<Settlement>;
    }
  };
}
