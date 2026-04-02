import type { PawnTicketModel } from "../../domain/model/pawn-ticket";

export function createLoadPawnTicketsService(
  adapter: { fetchPawnTickets: (tenantId: string) => Promise<PawnTicketModel[]> }
) {
  return function loadPawnTickets(tenantId: string) {
    return adapter.fetchPawnTickets(tenantId);
  };
}
