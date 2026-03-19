import { computed, defineComponent, onMounted, reactive, ref } from "vue";
import { authStore } from "../../stores/auth";
import { tenantStore } from "../../stores/tenant";
import {
  announceAuction,
  closeAuction,
  createAuction,
  fetchAuctions,
  fetchSurplusCases,
  openAuction,
  placeAuctionBid,
  settleAuctionLot
} from "../../services/api/auction";
import template from "./template.html?raw";
import "./styles.scss";

function emptyLot() {
  return {
    contractNumber: "",
    itemNumber: "",
    description: "",
    estimatedValue: "0.00",
    outstandingClaim: "0.00"
  };
}

export default defineComponent({
  name: "AuctionsView",
  setup() {
    const auctions = ref([]);
    const surplusCases = ref([]);
    const selectedAuctionId = ref("");
    const errorMessage = ref("");
    const createForm = reactive({
      title: "Versteigerung Berlin",
      location: "Berlin",
      lots: [emptyLot()]
    });
    const announceForm = reactive({
      auctionDate: "",
      announcementReference: ""
    });
    const bidForms = reactive({});
    const settleForms = reactive({});

    const selectedAuction = computed(() =>
      auctions.value.find((auction) => auction.id === selectedAuctionId.value) ?? null
    );

    function ensureLotState(lotId) {
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
      auctions.value = await fetchAuctions(tenantStore.selectedTenantId, authStore.token);
      surplusCases.value = await fetchSurplusCases(tenantStore.selectedTenantId, authStore.token);

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
        errorMessage.value = error instanceof Error ? error.message : "Request failed";
      }
    }

    async function submitCreate() {
      try {
        errorMessage.value = "";
        await createAuction(
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
        await reloadData();
      } catch (error) {
        errorMessage.value = error instanceof Error ? error.message : "Request failed";
      }
    }

    async function submitAnnouncement() {
      if (!selectedAuction.value) return;
      try {
        await announceAuction(
          tenantStore.selectedTenantId,
          selectedAuction.value.id,
          announceForm,
          authStore.token
        );
        await reloadData();
      } catch (error) {
        errorMessage.value = error instanceof Error ? error.message : "Request failed";
      }
    }

    async function triggerOpen() {
      if (!selectedAuction.value) return;
      await openAuction(tenantStore.selectedTenantId, selectedAuction.value.id, authStore.token);
      await reloadData();
    }

    async function triggerClose() {
      if (!selectedAuction.value) return;
      await closeAuction(tenantStore.selectedTenantId, selectedAuction.value.id, authStore.token);
      await reloadData();
    }

    async function submitBid(lotId) {
      if (!selectedAuction.value) return;
      try {
        await placeAuctionBid(
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
        await reloadData();
      } catch (error) {
        errorMessage.value = error instanceof Error ? error.message : "Request failed";
      }
    }

    async function submitSettlement(lotId) {
      if (!selectedAuction.value) return;
      try {
        await settleAuctionLot(
          tenantStore.selectedTenantId,
          selectedAuction.value.id,
          lotId,
          {
            hammerPrice: Number(settleForms[lotId].hammerPrice)
          },
          authStore.token
        );
        settleForms[lotId].hammerPrice = "";
        await reloadData();
      } catch (error) {
        errorMessage.value = error instanceof Error ? error.message : "Request failed";
      }
    }

    function addLot() {
      createForm.lots.push(emptyLot());
    }

    onMounted(reloadData);

    return {
      addLot,
      announceForm,
      auctions,
      bidForms,
      createForm,
      errorMessage,
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
  },
  template
});
