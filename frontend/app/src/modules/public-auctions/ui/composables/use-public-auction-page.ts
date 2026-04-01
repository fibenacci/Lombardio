import { onBeforeUnmount, reactive, ref } from "vue";
import { createHttpPublicAuctionsAdapter } from "../../infrastructure/adapters/http-public-auctions.adapter";

export function usePublicAuctionPage({
  route,
  t
}: {
  route: { params: Record<string, unknown> };
  t: (key: string, params?: Record<string, unknown>) => string;
}) {
  const adapter = createHttpPublicAuctionsAdapter();
  const auction = ref<any | null>(null);
  const bidder = ref<any | null>(null);
  const errorMessage = ref("");
  const realtimeState = ref("idle");
  const realtimeEvents = ref<any[]>([]);
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

  async function loadAuction() {
    auction.value = await adapter.fetchPublicOnlineAuction(String(route.params.tenantId ?? ""), String(route.params.auctionId ?? ""));
    if (!bidForm.lotId && auction.value?.lots?.length) {
      bidForm.lotId = auction.value.lots[0].id;
    }
  }

  async function reloadAuction() {
    try {
      errorMessage.value = "";
      await loadAuction();
    } catch (error) {
      errorMessage.value = error instanceof Error ? error.message : t("common.requestFailed");
    }
  }

  async function registerBidder() {
    bidder.value = await adapter.registerPublicBidder(
      String(route.params.tenantId ?? ""),
      String(route.params.auctionId ?? ""),
      registrationForm
    );
    await reloadAuction();
  }

  async function enableRealtime() {
    if (!bidder.value?.accessToken) {
      return;
    }
    const session = await adapter.createRealtimeSession(
      String(route.params.tenantId ?? ""),
      String(route.params.auctionId ?? ""),
      {
        accessToken: bidder.value.accessToken
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
    if (!bidder.value?.accessToken) {
      errorMessage.value = t("publicAuction.messages.registerFirst");
      return;
    }
    auction.value = await adapter.placePublicBid(
      String(route.params.tenantId ?? ""),
      String(route.params.auctionId ?? ""),
      {
        accessToken: bidder.value.accessToken,
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

  function getBidderCheckSummary(currentBidder: any) {
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
