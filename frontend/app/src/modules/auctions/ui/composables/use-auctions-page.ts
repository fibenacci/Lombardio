import { computed, reactive, ref } from "vue";
import { getRequestErrorMessage } from "../../../../shared/kernel/errors/request-error";
import { useAppToast } from "../../../../shared/ui/composables/use-app-toast";
import { createHttpAuctionsAdapter } from "../../infrastructure/adapters/http-auctions.adapter";

type AuctionLotDraft = {
  contractNumber: string;
  itemNumber: string;
  description: string;
  estimatedValue: string;
  outstandingClaim: string;
};

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

function emptyLot() {
  return {
    contractNumber: "",
    itemNumber: "",
    description: "",
    estimatedValue: "0.00",
    outstandingClaim: "0.00"
  } satisfies AuctionLotDraft;
}

function isPositiveAmount(value: string | number) {
  return Number(value) > 0;
}

function hasText(value: string) {
  return String(value).trim().length > 0;
}

function hasValidLotDraft(lot: AuctionLotDraft) {
  return hasText(lot.contractNumber)
    && hasText(lot.itemNumber)
    && hasText(lot.description)
    && isPositiveAmount(lot.estimatedValue)
    && isPositiveAmount(lot.outstandingClaim);
}

export function useAuctionsPage({
  t,
  tenantStore
}: {
  t: (key: string, params?: Record<string, unknown>) => string;
  tenantStore: { selectedTenantId: string };
}) {
  const adapter = createHttpAuctionsAdapter();
  const toast = useAppToast();
  const auctions = ref<Auction[]>([]);
  const surplusCases = ref<SurplusCase[]>([]);
  const selectedAuctionId = ref("");
  const errorMessage = ref("");
  const createForm = reactive({
    title: "",
    location: "",
    lots: [emptyLot()]
  });
  const announceForm = reactive({
    auctionDate: "",
    announcementReference: ""
  });
  const bidForms = reactive<Record<string, { bidderDisplayName: string; amount: string }>>({});
  const settleForms = reactive<Record<string, { hammerPrice: string }>>({});

  const selectedAuction = computed(
    () => auctions.value.find((auction) => auction.id === selectedAuctionId.value) ?? null
  );

  function getAuctionStatusLabel(status: string) {
    return t(`auctions.status.${status}`);
  }

  function ensureLotState(lotId: string) {
    if (!bidForms[lotId]) {
      bidForms[lotId] = { bidderDisplayName: "", amount: "" };
    }
    if (!settleForms[lotId]) {
      settleForms[lotId] = { hammerPrice: "" };
    }
  }

  function findSelectedLot(lotId: string) {
    return selectedAuction.value?.lots?.find((lot: { id: string }) => lot.id === lotId) ?? null;
  }

  function canCreateAuction() {
    const hasAuctionHeader = hasText(createForm.title) && hasText(createForm.location);
    const hasLots = createForm.lots.length > 0;
    const hasValidLots = createForm.lots.every(hasValidLotDraft);

    return hasAuctionHeader && hasLots && hasValidLots;
  }

  function canRecordBid(lotId: string) {
    const lot = findSelectedLot(lotId);
    const isLiveAuction = selectedAuction.value?.status === "LIVE";
    const isOpenLot = lot?.status === "OPEN";
    const hasBidder = hasText(bidForms[lotId]?.bidderDisplayName ?? "");
    const hasPositiveBid = isPositiveAmount(bidForms[lotId]?.amount ?? 0);
    const exceedsCurrentBid = Number(bidForms[lotId]?.amount ?? 0) > Number(lot?.latestBidAmount ?? 0);

    return isLiveAuction && isOpenLot && hasBidder && hasPositiveBid && exceedsCurrentBid;
  }

  async function loadData() {
    if (!tenantStore.selectedTenantId) {
      auctions.value = [];
      surplusCases.value = [];
      return;
    }

    errorMessage.value = "";
    auctions.value = await adapter.fetchAuctions(tenantStore.selectedTenantId);
    surplusCases.value = await adapter.fetchSurplusCases(tenantStore.selectedTenantId);

    if (!selectedAuctionId.value && auctions.value.length > 0) {
      selectedAuctionId.value = auctions.value[0].id;
    }
    for (const auction of auctions.value) {
      for (const lot of auction.lots) {
        ensureLotState(lot.id);
      }
    }
  }

  async function reloadData() {
    try {
      await loadData();
    } catch (error) {
      errorMessage.value = getRequestErrorMessage(error, t("common.requestFailed"));
    }
  }

  async function submitCreate() {
    try {
      errorMessage.value = "";
      if (!canCreateAuction()) {
        errorMessage.value = t("auctions.messages.invalidCreateForm");
        return;
      }
      await adapter.createAuction(
        tenantStore.selectedTenantId,
        {
          title: createForm.title,
          location: createForm.location,
          lots: createForm.lots.map((lot) => ({
            ...lot,
            estimatedValue: Number(lot.estimatedValue),
            outstandingClaim: Number(lot.outstandingClaim)
          }))
        },
      );
      createForm.lots = [emptyLot()];
      toast.success(t("auctions.messages.createdTitle"), createForm.title || t("auctions.messages.createdFallback"));
      await reloadData();
    } catch (error) {
      errorMessage.value = getRequestErrorMessage(error, t("common.requestFailed"));
    }
  }

  async function submitAnnouncement() {
    if (!selectedAuction.value) return;
    try {
      await adapter.announceAuction(tenantStore.selectedTenantId, selectedAuction.value.id, announceForm);
      toast.success(t("auctions.messages.announcementSavedTitle"), t("auctions.messages.announcementSavedToast"));
      await reloadData();
    } catch (error) {
      errorMessage.value = getRequestErrorMessage(error, t("common.requestFailed"));
    }
  }

  async function triggerOpen() {
    if (!selectedAuction.value) return;
    await adapter.openAuction(tenantStore.selectedTenantId, selectedAuction.value.id);
    toast.info(t("auctions.messages.openedTitle"), t("auctions.messages.openedToast", { title: selectedAuction.value.title }));
    await reloadData();
  }

  async function triggerClose() {
    if (!selectedAuction.value) return;
    await adapter.closeAuction(tenantStore.selectedTenantId, selectedAuction.value.id);
    toast.info(t("auctions.messages.closedTitle"), t("auctions.messages.closedToast", { title: selectedAuction.value.title }));
    await reloadData();
  }

  async function submitBid(lotId: string) {
    if (!selectedAuction.value) return;
    try {
      errorMessage.value = "";
      const lot = findSelectedLot(lotId);
      if (selectedAuction.value.status !== "LIVE") {
        errorMessage.value = t("auctions.messages.auctionMustBeLive");
        return;
      }
      if (!lot || lot.status !== "OPEN") {
        errorMessage.value = t("auctions.messages.lotMustBeOpen");
        return;
      }
      if (!hasText(bidForms[lotId].bidderDisplayName ?? "")) {
        errorMessage.value = t("auctions.messages.bidderRequired");
        return;
      }
      if (!isPositiveAmount(bidForms[lotId].amount)) {
        errorMessage.value = t("auctions.messages.bidAmountRequired");
        return;
      }
      if (Number(bidForms[lotId].amount) <= Number(lot.latestBidAmount ?? 0)) {
        errorMessage.value = t("auctions.messages.bidMustExceedCurrent");
        return;
      }
      await adapter.placeAuctionBid(
        tenantStore.selectedTenantId,
        selectedAuction.value.id,
        lotId,
        {
          bidderDisplayName: bidForms[lotId].bidderDisplayName,
          amount: Number(bidForms[lotId].amount)
        },
      );
      bidForms[lotId].amount = "";
      toast.success(t("auctions.messages.bidRecordedTitle"), t("auctions.messages.bidRecordedToast"));
      await reloadData();
    } catch (error) {
      errorMessage.value = getRequestErrorMessage(error, t("common.requestFailed"));
    }
  }

  async function submitSettlement(lotId: string) {
    if (!selectedAuction.value) return;
    try {
      await adapter.settleAuctionLot(
        tenantStore.selectedTenantId,
        selectedAuction.value.id,
        lotId,
        {
          hammerPrice: Number(settleForms[lotId].hammerPrice)
        },
      );
      settleForms[lotId].hammerPrice = "";
      toast.success(t("auctions.messages.lotSettledTitle"), t("auctions.messages.lotSettledToast"));
      await reloadData();
    } catch (error) {
      errorMessage.value = getRequestErrorMessage(error, t("common.requestFailed"));
    }
  }

  function addLot() {
    createForm.lots.push(emptyLot());
  }

  return {
    addLot,
    announceForm,
    auctions,
    bidForms,
    canCreateAuction,
    canRecordBid,
    createForm,
    errorMessage,
    getAuctionStatusLabel,
    loadData,
    reloadData,
    selectedAuction,
    selectedAuctionId,
    settleForms,
    submitAnnouncement,
    submitBid,
    submitCreate,
    submitSettlement,
    surplusCases,
    triggerClose,
    triggerOpen
  };
}
