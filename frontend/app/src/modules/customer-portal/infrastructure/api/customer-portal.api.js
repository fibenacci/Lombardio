import { BASE_URLS, createApiClient } from "../../../../shared/kernel/http/runtime-api-client";

const apiClient = createApiClient(BASE_URLS.customer);

export function fetchPortalInvitation(token) {
  return apiClient.get(`/api/v1/customer-portal/invitations/${encodeURIComponent(token)}`);
}

export function acceptPortalInvitation(payload) {
  return apiClient.post("/api/v1/customer-portal/invitations/accept", payload);
}

export function loginCustomerPortal(payload) {
  return apiClient.post("/api/v1/customer-portal/auth/login", payload);
}

export function fetchCustomerPortalMe(token) {
  return apiClient.get("/api/v1/customer-portal/auth/me", token);
}

export function fetchCustomerPortalPawnTickets(token) {
  return apiClient.get("/api/v1/customer-portal/pawn-tickets", token);
}

export function fetchCustomerPortalDocument(ticketNumber, token) {
  return apiClient.getBlob(
    `/api/v1/customer-portal/pawn-tickets/${encodeURIComponent(ticketNumber)}/document`,
    token
  );
}
