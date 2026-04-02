import { BASE_URLS, createApiClient } from "../../../../shared/kernel/http/runtime-api-client";

const apiClient = createApiClient(BASE_URLS.platform);

export function fetchOnlineAuctions(tenantId) {
  return apiClient.get(`/api/v1/platform/operator/tenants/${tenantId}/online-auctions`);
}

export function createOnlineAuction(tenantId, payload) {
  return apiClient.post(`/api/v1/platform/operator/tenants/${tenantId}/online-auctions`, payload);
}

export function reviewOnlineAuctionRegistration(tenantId, auctionId, registrationId, decision) {
  return apiClient.post(
    `/api/v1/platform/operator/tenants/${tenantId}/online-auctions/${auctionId}/registrations/${registrationId}/review`,
    { decision }
  );
}

export function publishOnlineAuction(tenantId, auctionId) {
  return apiClient.post(
    `/api/v1/platform/operator/tenants/${tenantId}/online-auctions/${auctionId}/publish`,
    {}
  );
}

export function startOnlineAuction(tenantId, auctionId) {
  return apiClient.post(
    `/api/v1/platform/operator/tenants/${tenantId}/online-auctions/${auctionId}/start`,
    {}
  );
}

export function closeOnlineAuction(tenantId, auctionId) {
  return apiClient.post(
    `/api/v1/platform/operator/tenants/${tenantId}/online-auctions/${auctionId}/close`,
    {}
  );
}

export function fetchPublicOnlineAuction(tenantId, auctionId) {
  const publicApiClient = createApiClient(BASE_URLS.publicOnlineAuction);
  return publicApiClient.get(`/api/public/tenants/${tenantId}/online-auctions/${auctionId}`);
}

export function registerPublicBidder(tenantId, auctionId, payload) {
  const publicApiClient = createApiClient(BASE_URLS.publicOnlineAuction);
  return publicApiClient.post(`/api/public/tenants/${tenantId}/online-auctions/${auctionId}/registrations`, payload);
}

export function placePublicBid(tenantId, auctionId, payload) {
  const publicApiClient = createApiClient(BASE_URLS.publicOnlineAuction);
  return publicApiClient.post(`/api/public/tenants/${tenantId}/online-auctions/${auctionId}/bids`, payload);
}

export function createRealtimeSession(tenantId, auctionId, payload) {
  const publicApiClient = createApiClient(BASE_URLS.publicOnlineAuction);
  return publicApiClient.post(
    `/api/public/tenants/${tenantId}/online-auctions/${auctionId}/realtime-session`,
    payload
  );
}
