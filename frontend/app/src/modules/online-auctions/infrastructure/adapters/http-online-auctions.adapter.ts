import * as onlineAuctionApi from "../api/online-auction.api";

export function createHttpOnlineAuctionsAdapter() {
  return {
    closeOnlineAuction(tenantId: string, auctionId: string, token: string) {
      return onlineAuctionApi.closeOnlineAuction(tenantId, auctionId, token);
    },
    createOnlineAuction(tenantId: string, payload: object, token: string) {
      return onlineAuctionApi.createOnlineAuction(tenantId, payload, token);
    },
    fetchOnlineAuctions(tenantId: string, token: string) {
      return onlineAuctionApi.fetchOnlineAuctions(tenantId, token);
    },
    publishOnlineAuction(tenantId: string, auctionId: string, token: string) {
      return onlineAuctionApi.publishOnlineAuction(tenantId, auctionId, token);
    },
    reviewOnlineAuctionRegistration(
      tenantId: string,
      auctionId: string,
      registrationId: string,
      payload: object,
      token: string
    ) {
      return onlineAuctionApi.reviewOnlineAuctionRegistration(tenantId, auctionId, registrationId, payload, token);
    },
    startOnlineAuction(tenantId: string, auctionId: string, token: string) {
      return onlineAuctionApi.startOnlineAuction(tenantId, auctionId, token);
    }
  };
}
