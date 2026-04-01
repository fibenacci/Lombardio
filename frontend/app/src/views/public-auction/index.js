import { defineComponent, onBeforeUnmount, onMounted, reactive, ref } from "vue";
import { useRoute } from "vue-router";
import {
  createRealtimeSession,
  fetchPublicOnlineAuction,
  placePublicBid,
  registerPublicBidder
} from "../../services/api/onlineAuction";
import { connectToAuctionRealtime } from "../../services/api/centrifugo";
import { useI18n } from "../../i18n";
import { useFormatters } from "../../utils/formatters";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "PublicAuctionView",
  setup() {
    const route = useRoute();
    const { t } = useI18n();
    const { formatCurrency, formatDateTime } = useFormatters();
    const auction = ref(null);
    const bidder = ref(null);
    const errorMessage = ref("");
    const realtimeState = ref("idle");
    const realtimeEvents = ref([]);
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
    let closeRealtime = null;

    async function loadAuction() {
      auction.value = await fetchPublicOnlineAuction(route.params.tenantId, route.params.auctionId);
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
      bidder.value = await registerPublicBidder(route.params.tenantId, route.params.auctionId, registrationForm);
      await reloadAuction();
    }

    async function enableRealtime() {
      if (!bidder.value?.accessToken) {
        return;
      }
      const session = await createRealtimeSession(route.params.tenantId, route.params.auctionId, {
        accessToken: bidder.value.accessToken
      });
      closeRealtime?.();
      closeRealtime = connectToAuctionRealtime(session, {
        onState(state) {
          realtimeState.value = state;
        },
        onPublication(publication) {
          realtimeEvents.value = [publication, ...realtimeEvents.value].slice(0, 8);
          reloadAuction();
        }
      });
    }

    async function submitBid() {
      if (!bidder.value?.accessToken) {
        errorMessage.value = t("publicAuction.messages.registerFirst");
        return;
      }
      auction.value = await placePublicBid(route.params.tenantId, route.params.auctionId, {
        accessToken: bidder.value.accessToken,
        lotId: bidForm.lotId,
        amount: Number(bidForm.amount)
      });
      bidForm.amount = "";
    }

    function getApprovalStatusLabel(status) {
      return t(`publicAuction.status.approval.${status}`);
    }

    function getRealtimeStateLabel(state) {
      return t(`publicAuction.status.realtime.${state}`);
    }

    function getBidderCheckSummary(currentBidder) {
      return t("publicAuction.bidderChecks", {
        kyc: t(`onlineAuctions.registrationStatus.kyc.${currentBidder.kycStatus}`),
        account: t(`onlineAuctions.registrationStatus.account.${currentBidder.accountCheckStatus}`)
      });
    }

    onMounted(reloadAuction);
    onBeforeUnmount(() => closeRealtime?.());

    return {
      auction,
      bidForm,
      bidder,
      enableRealtime,
      errorMessage,
      formatCurrency,
      formatDateTime,
      getApprovalStatusLabel,
      getBidderCheckSummary,
      getRealtimeStateLabel,
      realtimeEvents,
      realtimeState,
      registerBidder,
      registrationForm,
      submitBid,
      t
    };
  },
  template
});
