import * as onlineAuctionApi from "../../../online-auctions/infrastructure/api/online-auction.api";
import { connectToAuctionRealtime } from "../realtime/centrifugo.client";

type PublicAuctionLot = {
  id: string;
};

type PublicAuction = {
  lots?: PublicAuctionLot[];
};

type PublicBidder = {
  accessToken?: string;
  accountCheckStatus?: string;
  kycStatus?: string;
} | null;

export function createHttpPublicAuctionsAdapter() {
  return {
    connectToAuctionRealtime,
    createRealtimeSession(tenantId: string, auctionId: string, payload: object) {
      return onlineAuctionApi.createRealtimeSession(tenantId, auctionId, payload);
    },
    fetchPublicOnlineAuction(tenantId: string, auctionId: string): Promise<PublicAuction> {
      return onlineAuctionApi.fetchPublicOnlineAuction(tenantId, auctionId) as Promise<PublicAuction>;
    },
    placePublicBid(tenantId: string, auctionId: string, payload: object): Promise<PublicAuction> {
      return onlineAuctionApi.placePublicBid(tenantId, auctionId, payload) as Promise<PublicAuction>;
    },
    registerPublicBidder(tenantId: string, auctionId: string, payload: object): Promise<PublicBidder> {
      return onlineAuctionApi.registerPublicBidder(tenantId, auctionId, payload) as Promise<PublicBidder>;
    }
  };
}
