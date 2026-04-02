import { BASE_URLS, createApiClient } from "../../../../shared/kernel/http/runtime-api-client";

const apiClient = createApiClient(BASE_URLS.platform);

export function fetchAuctions(tenantId) {
  return apiClient.get(`/api/v1/platform/operator/tenants/${tenantId}/auctions`);
}

export function createAuction(tenantId, payload) {
  return apiClient.post(`/api/v1/platform/operator/tenants/${tenantId}/auctions`, payload);
}

export function announceAuction(tenantId, auctionId, payload) {
  return apiClient.post(
    `/api/v1/platform/operator/tenants/${tenantId}/auctions/${auctionId}/announce`,
    payload
  );
}

export function openAuction(tenantId, auctionId) {
  return apiClient.post(`/api/v1/platform/operator/tenants/${tenantId}/auctions/${auctionId}/open`, {});
}

export function closeAuction(tenantId, auctionId) {
  return apiClient.post(`/api/v1/platform/operator/tenants/${tenantId}/auctions/${auctionId}/close`, {});
}

export function placeAuctionBid(tenantId, auctionId, lotId, payload) {
  return apiClient.post(
    `/api/v1/platform/operator/tenants/${tenantId}/auctions/${auctionId}/lots/${lotId}/bids`,
    payload
  );
}

export function settleAuctionLot(tenantId, auctionId, lotId, payload) {
  return apiClient.post(
    `/api/v1/platform/operator/tenants/${tenantId}/auctions/${auctionId}/lots/${lotId}/settle`,
    payload
  );
}

export function fetchSurplusCases(tenantId) {
  return apiClient.get(`/api/v1/platform/operator/tenants/${tenantId}/surplus-cases`);
}
