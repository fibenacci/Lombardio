import * as auctionApi from "../api/auction.api";

type AuctionLot = {
  id: string;
  latestBidAmount?: number | string | null;
  status?: string | null;
};

type Auction = {
  id: string;
  lots: AuctionLot[];
  status?: string | null;
  title?: string;
};

type SurplusCase = {
  contractNumber?: string;
  itemNumber?: string;
};

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
    fetchAuctions(tenantId: string): Promise<Auction[]> {
      return auctionApi.fetchAuctions(tenantId) as Promise<Auction[]>;
    },
    fetchSurplusCases(tenantId: string): Promise<SurplusCase[]> {
      return auctionApi.fetchSurplusCases(tenantId) as Promise<SurplusCase[]>;
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
