import type { PawnTicketModel } from "../../domain/model/pawn-ticket";

export function createLoadPawnTicketsService(
  adapter: { fetchPawnTickets: (tenantId: string, token: string) => Promise<PawnTicketModel[]> }
) {
  return function loadPawnTickets(tenantId: string, token: string) {
    return adapter.fetchPawnTickets(tenantId, token);
  };
}
