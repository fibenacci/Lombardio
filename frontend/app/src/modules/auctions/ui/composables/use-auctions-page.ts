import { computed, reactive, ref } from "vue";
import { useAppToast } from "../../../../shared/ui/composables/use-app-toast";
import { createHttpAuctionsAdapter } from "../../infrastructure/adapters/http-auctions.adapter";

function emptyLot() {
  return {
    contractNumber: "",
    itemNumber: "",
    description: "",
    estimatedValue: "0.00",
    outstandingClaim: "0.00"
  };
}

export function useAuctionsPage({
  authStore,
  t,
  tenantStore
}: {
  authStore: { token: string };
  t: (key: string, params?: Record<string, unknown>) => string;
  tenantStore: { selectedTenantId: string };
}) {
  const adapter = createHttpAuctionsAdapter();
  const toast = useAppToast();
  const auctions = ref<any[]>([]);
  const surplusCases = ref<any[]>([]);
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

  async function loadData() {
    if (!tenantStore.selectedTenantId) {
      auctions.value = [];
      surplusCases.value = [];
      return;
    }

    errorMessage.value = "";
    auctions.value = await adapter.fetchAuctions(tenantStore.selectedTenantId, authStore.token);
    surplusCases.value = await adapter.fetchSurplusCases(tenantStore.selectedTenantId, authStore.token);

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
      errorMessage.value = error instanceof Error ? error.message : t("common.requestFailed");
    }
  }

  async function submitCreate() {
    try {
      errorMessage.value = "";
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
        authStore.token
      );
      createForm.lots = [emptyLot()];
      toast.success(t("auctions.messages.createdTitle"), createForm.title || t("auctions.messages.createdFallback"));
      await reloadData();
    } catch (error) {
      errorMessage.value = error instanceof Error ? error.message : t("common.requestFailed");
    }
  }

  async function submitAnnouncement() {
    if (!selectedAuction.value) return;
    try {
      await adapter.announceAuction(tenantStore.selectedTenantId, selectedAuction.value.id, announceForm, authStore.token);
      toast.success(t("auctions.messages.announcementSavedTitle"), t("auctions.messages.announcementSavedToast"));
      await reloadData();
    } catch (error) {
      errorMessage.value = error instanceof Error ? error.message : t("common.requestFailed");
    }
  }

  async function triggerOpen() {
    if (!selectedAuction.value) return;
    await adapter.openAuction(tenantStore.selectedTenantId, selectedAuction.value.id, authStore.token);
    toast.info(t("auctions.messages.openedTitle"), t("auctions.messages.openedToast", { title: selectedAuction.value.title }));
    await reloadData();
  }

  async function triggerClose() {
    if (!selectedAuction.value) return;
    await adapter.closeAuction(tenantStore.selectedTenantId, selectedAuction.value.id, authStore.token);
    toast.info(t("auctions.messages.closedTitle"), t("auctions.messages.closedToast", { title: selectedAuction.value.title }));
    await reloadData();
  }

  async function submitBid(lotId: string) {
    if (!selectedAuction.value) return;
    try {
      await adapter.placeAuctionBid(
        tenantStore.selectedTenantId,
        selectedAuction.value.id,
        lotId,
        {
          bidderDisplayName: bidForms[lotId].bidderDisplayName,
          amount: Number(bidForms[lotId].amount)
        },
        authStore.token
      );
      bidForms[lotId].amount = "";
      toast.success(t("auctions.messages.bidRecordedTitle"), t("auctions.messages.bidRecordedToast"));
      await reloadData();
    } catch (error) {
      errorMessage.value = error instanceof Error ? error.message : t("common.requestFailed");
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
        authStore.token
      );
      settleForms[lotId].hammerPrice = "";
      toast.success(t("auctions.messages.lotSettledTitle"), t("auctions.messages.lotSettledToast"));
      await reloadData();
    } catch (error) {
      errorMessage.value = error instanceof Error ? error.message : t("common.requestFailed");
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
