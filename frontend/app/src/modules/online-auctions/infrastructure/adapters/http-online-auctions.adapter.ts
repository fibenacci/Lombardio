import * as onlineAuctionApi from "../api/online-auction.api";

export function createHttpOnlineAuctionsAdapter() {
  return {
    closeOnlineAuction(tenantId: string, auctionId: string) {
      return onlineAuctionApi.closeOnlineAuction(tenantId, auctionId);
    },
    createOnlineAuction(tenantId: string, payload: object) {
      return onlineAuctionApi.createOnlineAuction(tenantId, payload);
    },
    fetchOnlineAuctions(tenantId: string) {
      return onlineAuctionApi.fetchOnlineAuctions(tenantId);
    },
    publishOnlineAuction(tenantId: string, auctionId: string) {
      return onlineAuctionApi.publishOnlineAuction(tenantId, auctionId);
    },
    reviewOnlineAuctionRegistration(
      tenantId: string,
      auctionId: string,
      registrationId: string,
      payload: object
    ) {
      return onlineAuctionApi.reviewOnlineAuctionRegistration(tenantId, auctionId, registrationId, payload);
    },
    startOnlineAuction(tenantId: string, auctionId: string) {
      return onlineAuctionApi.startOnlineAuction(tenantId, auctionId);
    }
  };
}
