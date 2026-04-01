import { BASE_URLS, createApiClient } from "../../../../shared/kernel/http/runtime-api-client";

const apiClient = createApiClient(BASE_URLS.onlineAuction);

export function fetchOnlineAuctions(tenantId, token) {
  return apiClient.get(`/api/v1/tenants/${tenantId}/online-auctions`, token);
}

export function createOnlineAuction(tenantId, payload, token) {
  return apiClient.post(`/api/v1/tenants/${tenantId}/online-auctions`, payload, token);
}

export function reviewOnlineAuctionRegistration(tenantId, auctionId, registrationId, decision, token) {
  return apiClient.post(
    `/api/v1/tenants/${tenantId}/online-auctions/${auctionId}/registrations/${registrationId}/review`,
    { decision },
    token
  );
}

export function publishOnlineAuction(tenantId, auctionId, token) {
  return apiClient.post(`/api/v1/tenants/${tenantId}/online-auctions/${auctionId}/publish`, {}, token);
}

export function startOnlineAuction(tenantId, auctionId, token) {
  return apiClient.post(`/api/v1/tenants/${tenantId}/online-auctions/${auctionId}/start`, {}, token);
}

export function closeOnlineAuction(tenantId, auctionId, token) {
  return apiClient.post(`/api/v1/tenants/${tenantId}/online-auctions/${auctionId}/close`, {}, token);
}

export function fetchPublicOnlineAuction(tenantId, auctionId) {
  return apiClient.get(`/api/public/tenants/${tenantId}/online-auctions/${auctionId}`);
}

export function registerPublicBidder(tenantId, auctionId, payload) {
  return apiClient.post(`/api/public/tenants/${tenantId}/online-auctions/${auctionId}/registrations`, payload);
}

export function placePublicBid(tenantId, auctionId, payload) {
  return apiClient.post(`/api/public/tenants/${tenantId}/online-auctions/${auctionId}/bids`, payload);
}

export function createRealtimeSession(tenantId, auctionId, payload) {
  return apiClient.post(
    `/api/public/tenants/${tenantId}/online-auctions/${auctionId}/realtime-session`,
    payload
  );
}
