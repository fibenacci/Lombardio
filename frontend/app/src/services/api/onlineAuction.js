import { onlineAuctionGet, onlineAuctionPost } from "./client";

export function fetchOnlineAuctions(tenantId, token) {
  return onlineAuctionGet(`/api/v1/tenants/${tenantId}/online-auctions`, token);
}

export function createOnlineAuction(tenantId, payload, token) {
  return onlineAuctionPost(`/api/v1/tenants/${tenantId}/online-auctions`, payload, token);
}

export function reviewOnlineAuctionRegistration(tenantId, auctionId, registrationId, decision, token) {
  return onlineAuctionPost(
    `/api/v1/tenants/${tenantId}/online-auctions/${auctionId}/registrations/${registrationId}/review`,
    { decision },
    token
  );
}

export function publishOnlineAuction(tenantId, auctionId, token) {
  return onlineAuctionPost(`/api/v1/tenants/${tenantId}/online-auctions/${auctionId}/publish`, {}, token);
}

export function startOnlineAuction(tenantId, auctionId, token) {
  return onlineAuctionPost(`/api/v1/tenants/${tenantId}/online-auctions/${auctionId}/start`, {}, token);
}

export function closeOnlineAuction(tenantId, auctionId, token) {
  return onlineAuctionPost(`/api/v1/tenants/${tenantId}/online-auctions/${auctionId}/close`, {}, token);
}

export function fetchPublicOnlineAuction(tenantId, auctionId) {
  return onlineAuctionGet(`/api/public/tenants/${tenantId}/online-auctions/${auctionId}`);
}

export function registerPublicBidder(tenantId, auctionId, payload) {
  return onlineAuctionPost(`/api/public/tenants/${tenantId}/online-auctions/${auctionId}/registrations`, payload);
}

export function placePublicBid(tenantId, auctionId, payload) {
  return onlineAuctionPost(`/api/public/tenants/${tenantId}/online-auctions/${auctionId}/bids`, payload);
}

export function createRealtimeSession(tenantId, auctionId, payload) {
  return onlineAuctionPost(`/api/public/tenants/${tenantId}/online-auctions/${auctionId}/realtime-session`, payload);
}
