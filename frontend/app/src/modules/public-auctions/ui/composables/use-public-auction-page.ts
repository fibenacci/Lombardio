import { onBeforeUnmount, reactive, ref } from "vue";
import { getRequestErrorMessage } from "../../../../shared/kernel/errors/request-error";
import { createHttpPublicAuctionsAdapter } from "../../infrastructure/adapters/http-public-auctions.adapter";

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

function hasText(value: string) {
  return String(value).trim().length > 0;
}

export function usePublicAuctionPage({
  route,
  t
}: {
  route: { params: Record<string, unknown> };
  t: (key: string, params?: Record<string, unknown>) => string;
}) {
  const adapter = createHttpPublicAuctionsAdapter();
  const auction = ref<PublicAuction | null>(null);
  const bidder = ref<PublicBidder>(null);
  const errorMessage = ref("");
  const realtimeState = ref("idle");
  const realtimeEvents = ref<unknown[]>([]);
  const registrationForm = reactive({
    displayName: "",
    email: "",
    legalName: "",
    birthDate: "",
    iban: ""
  });
  const bidForm = reactive({
    lotId: "",
    amount: ""
  });
  let closeRealtime: null | (() => void) = null;

  function tenantId() {
    return String(route.params.tenantId ?? "");
  }

  function auctionId() {
    return String(route.params.auctionId ?? "");
  }

  function bidderAccessToken() {
    return bidder.value?.accessToken ?? "";
  }

  function hasRegisteredBidder() {
    return hasText(bidderAccessToken());
  }

  function hasValidBidRequest() {
    return hasText(bidForm.lotId) && Number(bidForm.amount) > 0;
  }

  async function loadAuction() {
    auction.value = await adapter.fetchPublicOnlineAuction(tenantId(), auctionId());
    if (!bidForm.lotId && auction.value?.lots?.length) {
      bidForm.lotId = auction.value.lots[0].id;
    }
  }

  async function reloadAuction() {
    try {
      errorMessage.value = "";
      await loadAuction();
    } catch (error) {
      errorMessage.value = getRequestErrorMessage(error, t("common.requestFailed"));
    }
  }

  async function registerBidder() {
    bidder.value = await adapter.registerPublicBidder(
      tenantId(),
      auctionId(),
      registrationForm
    );
    await reloadAuction();
  }

  async function enableRealtime() {
    if (!hasRegisteredBidder()) {
      return;
    }
    const session = await adapter.createRealtimeSession(
      tenantId(),
      auctionId(),
      {
        accessToken: bidderAccessToken()
      }
    );
    closeRealtime?.();
    closeRealtime = adapter.connectToAuctionRealtime(session, {
      onPublication(publication: unknown) {
        realtimeEvents.value = [publication, ...realtimeEvents.value].slice(0, 8);
        reloadAuction();
      },
      onState(state: string) {
        realtimeState.value = state;
      }
    });
  }

  async function submitBid() {
    if (!hasRegisteredBidder()) {
      errorMessage.value = t("publicAuction.messages.registerFirst");
      return;
    }
    if (!hasValidBidRequest()) {
      errorMessage.value = t("publicAuction.messages.invalidBid");
      return;
    }
    auction.value = await adapter.placePublicBid(
      tenantId(),
      auctionId(),
      {
        accessToken: bidderAccessToken(),
        lotId: bidForm.lotId,
        amount: Number(bidForm.amount)
      }
    );
    bidForm.amount = "";
  }

  function getApprovalStatusLabel(status: string) {
    return t(`publicAuction.status.approval.${status}`);
  }

  function getRealtimeStateLabel(state: string) {
    return t(`publicAuction.status.realtime.${state}`);
  }

  function getBidderCheckSummary(currentBidder: NonNullable<PublicBidder>) {
    return t("publicAuction.bidderChecks", {
      account: t(`onlineAuctions.registrationStatus.account.${currentBidder.accountCheckStatus}`),
      kyc: t(`onlineAuctions.registrationStatus.kyc.${currentBidder.kycStatus}`)
    });
  }

  onBeforeUnmount(() => closeRealtime?.());

  return {
    auction,
    bidForm,
    bidder,
    enableRealtime,
    errorMessage,
    getApprovalStatusLabel,
    getBidderCheckSummary,
    getRealtimeStateLabel,
    loadAuction,
    realtimeEvents,
    realtimeState,
    registerBidder,
    registrationForm,
    reloadAuction,
    submitBid
  };
}
