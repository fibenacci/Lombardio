import { BASE_URLS, createApiClient } from "../../../../shared/kernel/http/runtime-api-client";

const apiClient = createApiClient(BASE_URLS.auction);

export function fetchAuctions(tenantId, token) {
  return apiClient.get(`/api/v1/tenants/${tenantId}/auctions`, token);
}

export function createAuction(tenantId, payload, token) {
  return apiClient.post(`/api/v1/tenants/${tenantId}/auctions`, payload, token);
}

export function announceAuction(tenantId, auctionId, payload, token) {
  return apiClient.post(`/api/v1/tenants/${tenantId}/auctions/${auctionId}/announce`, payload, token);
}

export function openAuction(tenantId, auctionId, token) {
  return apiClient.post(`/api/v1/tenants/${tenantId}/auctions/${auctionId}/open`, {}, token);
}

export function closeAuction(tenantId, auctionId, token) {
  return apiClient.post(`/api/v1/tenants/${tenantId}/auctions/${auctionId}/close`, {}, token);
}

export function placeAuctionBid(tenantId, auctionId, lotId, payload, token) {
  return apiClient.post(`/api/v1/tenants/${tenantId}/auctions/${auctionId}/lots/${lotId}/bids`, payload, token);
}

export function settleAuctionLot(tenantId, auctionId, lotId, payload, token) {
  return apiClient.post(
    `/api/v1/tenants/${tenantId}/auctions/${auctionId}/lots/${lotId}/settle`,
    payload,
    token
  );
}

export function fetchSurplusCases(tenantId, token) {
  return apiClient.get(`/api/v1/tenants/${tenantId}/surplus-cases`, token);
}
