import { pawnTicketGet, pawnTicketGetBlob, pawnTicketPost, pawnTicketRootPost } from "./client";

export function fetchPawnTicketQuote(payload, token) {
  return pawnTicketPost("/api/v1/pawn-tickets/quote", payload, token);
}

export function fetchPawnTickets(tenantId, token) {
  return pawnTicketGet(`/api/v1/tenants/${tenantId}/pawn-tickets`, token);
}

export function fetchPawnTicketDocument(ticketNumber, token) {
  return pawnTicketGetBlob(`/api/v1/pawn-tickets/${encodeURIComponent(ticketNumber)}/document`, token);
}

export function fetchPawnTicketLabels(ticketNumber, token) {
  return pawnTicketGetBlob(`/api/v1/pawn-tickets/${encodeURIComponent(ticketNumber)}/labels`, token);
}

export function extendPawnTicket(payload, token) {
  return pawnTicketPost("/api/v1/pawn-tickets/extend", payload, token);
}

export function redeemPawnTicket(payload, token) {
  return pawnTicketPost("/api/v1/pawn-tickets/redeem", payload, token);
}

export function calculatePartialRepayment(payload, token) {
  return pawnTicketPost("/api/v1/pawn-tickets/partial-repayment", payload, token);
}

export function executeCashTransaction(payload, token) {
  return pawnTicketRootPost("/api/v1/cash-transactions", payload, token);
}

export function fetchCashTransactions(tenantId, token) {
  return pawnTicketGet(`/api/v1/tenants/${tenantId}/cash-transactions`, token);
}
