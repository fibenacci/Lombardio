import * as onlineAuctionApi from "../api/online-auction.api";

type OnlineAuctionRegistration = {
  id: string;
  kycStatus?: string | null;
  accountCheckStatus?: string | null;
  reviewNote?: string | null;
};

type OnlineAuction = {
  id: string;
  tenantId: string;
  title: string;
  status?: string | null;
  registrations?: OnlineAuctionRegistration[];
};

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
