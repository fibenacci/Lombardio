import { customerGet, customerGetBlob, customerPost } from "./client";

export function fetchPortalInvitation(token) {
  return customerGet(`/api/v1/customer-portal/invitations/${encodeURIComponent(token)}`);
}

export function acceptPortalInvitation(payload) {
  return customerPost("/api/v1/customer-portal/invitations/accept", payload);
}

export function loginCustomerPortal(payload) {
  return customerPost("/api/v1/customer-portal/auth/login", payload);
}

export function fetchCustomerPortalMe(token) {
  return customerGet("/api/v1/customer-portal/auth/me", token);
}

export function fetchCustomerPortalPawnTickets(token) {
  return customerGet("/api/v1/customer-portal/pawn-tickets", token);
}

export function fetchCustomerPortalDocument(ticketNumber, token) {
  return customerGetBlob(`/api/v1/customer-portal/pawn-tickets/${encodeURIComponent(ticketNumber)}/document`, token);
}
