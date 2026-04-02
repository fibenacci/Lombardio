import * as pawnTicketApi from "../api/pawn-ticket.api";
import type { PawnTicketModel } from "../../domain/model/pawn-ticket";

export function createHttpPawnTicketsAdapter() {
  return {
    fetchPawnTicketDocument(ticketNumber: string) {
      return pawnTicketApi.fetchPawnTicketDocument(ticketNumber);
    },
    fetchPawnTicketLabels(ticketNumber: string) {
      return pawnTicketApi.fetchPawnTicketLabels(ticketNumber);
    },
    fetchPawnTickets(tenantId: string) {
      return (pawnTicketApi.fetchPawnTickets as (tenantId: string) => Promise<PawnTicketModel[]>)(tenantId);
    }
  };
}
