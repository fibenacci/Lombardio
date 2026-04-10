import * as onlineAuctionApi from "../api/online-auction.api";
import type { components } from "../api/types/online-auction";
import {
  OnlineAuctionResponseStatus as OnlineAuctionStatus,
  BidderRegistrationResponseKycStatus as BidderKycStatus,
  BidderRegistrationResponseAccountCheckStatus as BidderAccountStatus
} from "../api/types/online-auction";

export type OnlineAuctionRegistration = components["schemas"]["BidderRegistrationResponse"];
export type OnlineAuction = components["schemas"]["OnlineAuctionResponse"];

export { OnlineAuctionStatus, BidderKycStatus, BidderAccountStatus };

export function createHttpOnlineAuctionsAdapter() {
  return {
    closeOnlineAuction(tenantId: string, auctionId: string) {
      return onlineAuctionApi.closeOnlineAuction(tenantId, auctionId);
    },
    createOnlineAuction(tenantId: string, payload: object) {
      return onlineAuctionApi.createOnlineAuction(tenantId, payload);
    },
    fetchOnlineAuctions(tenantId: string): Promise<OnlineAuction[]> {
      return onlineAuctionApi.fetchOnlineAuctions(tenantId) as Promise<OnlineAuction[]>;
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
