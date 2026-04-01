import * as pawnTicketApi from "../api/pawn-ticket.api";
import type { PawnTicketModel } from "../../domain/model/pawn-ticket";

export function createHttpPawnTicketsAdapter() {
  return {
    fetchPawnTicketDocument(ticketNumber: string, token: string) {
      return pawnTicketApi.fetchPawnTicketDocument(ticketNumber, token);
    },
    fetchPawnTicketLabels(ticketNumber: string, token: string) {
      return pawnTicketApi.fetchPawnTicketLabels(ticketNumber, token);
    },
    fetchPawnTickets(tenantId: string, token: string) {
      return (pawnTicketApi.fetchPawnTickets as (tenantId: string, token: string) => Promise<PawnTicketModel[]>)(
        tenantId,
        token
      );
    }
  };
}
