import * as onlineAuctionApi from "../../../online-auctions/infrastructure/api/online-auction.api";
import { connectToAuctionRealtime } from "../realtime/centrifugo.client";

export function createHttpPublicAuctionsAdapter() {
  return {
    connectToAuctionRealtime,
    createRealtimeSession(tenantId: string, auctionId: string, payload: object) {
      return onlineAuctionApi.createRealtimeSession(tenantId, auctionId, payload);
    },
    fetchPublicOnlineAuction(tenantId: string, auctionId: string) {
      return onlineAuctionApi.fetchPublicOnlineAuction(tenantId, auctionId);
    },
    placePublicBid(tenantId: string, auctionId: string, payload: object) {
      return onlineAuctionApi.placePublicBid(tenantId, auctionId, payload);
    },
    registerPublicBidder(tenantId: string, auctionId: string, payload: object) {
      return onlineAuctionApi.registerPublicBidder(tenantId, auctionId, payload);
    }
  };
}
