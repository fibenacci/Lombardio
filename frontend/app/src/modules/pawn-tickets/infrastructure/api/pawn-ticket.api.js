import { BASE_URLS, createApiClient } from "../../../../shared/kernel/http/runtime-api-client";

const apiClient = createApiClient(BASE_URLS.platform);

export function fetchPawnTicketQuote(payload) {
  return apiClient.post("/api/v1/platform/operator/pawn-tickets/quote", payload);
}

export function fetchPawnTickets(tenantId) {
  return apiClient.get(`/api/v1/platform/operator/tenants/${tenantId}/pawn-tickets`);
}

export function fetchPawnTicketDocument(ticketNumber) {
  return apiClient.getBlob(`/api/v1/platform/operator/pawn-tickets/${encodeURIComponent(ticketNumber)}/document`);
}

export function fetchPawnTicketLabels(ticketNumber) {
  return apiClient.getBlob(`/api/v1/platform/operator/pawn-tickets/${encodeURIComponent(ticketNumber)}/labels`);
}

export function extendPawnTicket(payload) {
  return apiClient.post("/api/v1/platform/operator/pawn-tickets/extend", payload);
}

export function redeemPawnTicket(payload) {
  return apiClient.post("/api/v1/platform/operator/pawn-tickets/redeem", payload);
}

export function calculatePartialRepayment(payload) {
  return apiClient.post("/api/v1/platform/operator/pawn-tickets/partial-repayment", payload);
}

export function executeCashTransaction(payload) {
  return apiClient.post("/api/v1/platform/operator/cash-transactions", payload);
}

export function fetchCashTransactions(tenantId) {
  return apiClient.get(`/api/v1/platform/operator/tenants/${tenantId}/cash-transactions`);
}
