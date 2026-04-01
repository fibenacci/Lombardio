import * as auctionApi from "../api/auction.api";

export function createHttpAuctionsAdapter() {
  return {
    announceAuction(tenantId: string, auctionId: string, payload: object, token: string) {
      return auctionApi.announceAuction(tenantId, auctionId, payload, token);
    },
    closeAuction(tenantId: string, auctionId: string, token: string) {
      return auctionApi.closeAuction(tenantId, auctionId, token);
    },
    createAuction(tenantId: string, payload: object, token: string) {
      return auctionApi.createAuction(tenantId, payload, token);
    },
    fetchAuctions(tenantId: string, token: string) {
      return auctionApi.fetchAuctions(tenantId, token);
    },
    fetchSurplusCases(tenantId: string, token: string) {
      return auctionApi.fetchSurplusCases(tenantId, token);
    },
    openAuction(tenantId: string, auctionId: string, token: string) {
      return auctionApi.openAuction(tenantId, auctionId, token);
    },
    placeAuctionBid(tenantId: string, auctionId: string, lotId: string, payload: object, token: string) {
      return auctionApi.placeAuctionBid(tenantId, auctionId, lotId, payload, token);
    },
    settleAuctionLot(tenantId: string, auctionId: string, lotId: string, payload: object, token: string) {
      return auctionApi.settleAuctionLot(tenantId, auctionId, lotId, payload, token);
    }
  };
}
