import * as auctionApi from "../api/auction.api";

export function createHttpAuctionsAdapter() {
  return {
    announceAuction(tenantId: string, auctionId: string, payload: object) {
      return auctionApi.announceAuction(tenantId, auctionId, payload);
    },
    closeAuction(tenantId: string, auctionId: string) {
      return auctionApi.closeAuction(tenantId, auctionId);
    },
    createAuction(tenantId: string, payload: object) {
      return auctionApi.createAuction(tenantId, payload);
    },
    fetchAuctions(tenantId: string) {
      return auctionApi.fetchAuctions(tenantId);
    },
    fetchSurplusCases(tenantId: string) {
      return auctionApi.fetchSurplusCases(tenantId);
    },
    openAuction(tenantId: string, auctionId: string) {
      return auctionApi.openAuction(tenantId, auctionId);
    },
    placeAuctionBid(tenantId: string, auctionId: string, lotId: string, payload: object) {
      return auctionApi.placeAuctionBid(tenantId, auctionId, lotId, payload);
    },
    settleAuctionLot(tenantId: string, auctionId: string, lotId: string, payload: object) {
      return auctionApi.settleAuctionLot(tenantId, auctionId, lotId, payload);
    }
  };
}
