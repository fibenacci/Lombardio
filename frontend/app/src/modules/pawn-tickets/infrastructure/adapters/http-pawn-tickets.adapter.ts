import * as pawnTicketApi from "../api/pawn-ticket.api";
import type { PawnTicketModel, PawnTicketDto } from "../../domain/model/pawn-ticket";
import { CashTransactionResponseType as CashTransactionType } from "../api/types/pawn-ticket";

export { CashTransactionType };

export function createHttpPawnTicketsAdapter() {
  return {
    fetchPawnTicketDocument(ticketNumber: string) {
      return pawnTicketApi.fetchPawnTicketDocument(ticketNumber);
    },
    fetchPawnTicketLabels(ticketNumber: string) {
      return pawnTicketApi.fetchPawnTicketLabels(ticketNumber);
    },
    async fetchPawnTickets(tenantId: string): Promise<PawnTicketModel[]> {
      return (pawnTicketApi.fetchPawnTickets(tenantId) as Promise<PawnTicketDto[]>);
    }
  };
}
