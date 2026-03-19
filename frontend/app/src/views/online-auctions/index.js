import { computed, defineComponent, onMounted, reactive, ref } from "vue";
import { authStore } from "../../stores/auth";
import { tenantStore } from "../../stores/tenant";
import {
  closeOnlineAuction,
  createOnlineAuction,
  fetchOnlineAuctions,
  publishOnlineAuction,
  reviewOnlineAuctionRegistration,
  startOnlineAuction
} from "../../services/api/onlineAuction";
import { useAppToast } from "../../composables/use-app-toast";
import template from "./template.html?raw";
import "./styles.scss";

function emptyLot() {
  return { title: "", description: "", startingBid: "0.00" };
}

export default defineComponent({
  name: "OnlineAuctionsView",
  setup() {
    const toast = useAppToast();
    const auctions = ref([]);
    const selectedAuctionId = ref("");
    const errorMessage = ref("");
    const createForm = reactive({
      title: "Online Evening Sale",
      slug: "online-evening-sale",
      minimumIncrement: "10.00",
      countdownSeconds: "180",
      lots: [emptyLot()]
    });
    const reviewForms = reactive({});

    const selectedAuction = computed(() =>
      auctions.value.find((item) => item.id === selectedAuctionId.value) ?? null
    );

    async function loadData() {
      if (!tenantStore.selectedTenantId) {
        auctions.value = [];
        return;
      }
      auctions.value = await fetchOnlineAuctions(tenantStore.selectedTenantId, authStore.token);
      if (!selectedAuctionId.value && auctions.value.length > 0) {
        selectedAuctionId.value = auctions.value[0].id;
      }
      for (const auction of auctions.value) {
        for (const registration of auction.registrations ?? []) {
          if (!reviewForms[registration.id]) {
            reviewForms[registration.id] = {
              kycStatus: registration.kycStatus ?? "PENDING",
              accountCheckStatus: registration.accountCheckStatus ?? "PENDING",
              reviewNote: registration.reviewNote ?? ""
            };
          }
        }
      }
    }

    async function reloadData() {
      try {
        errorMessage.value = "";
        await loadData();
      } catch (error) {
        errorMessage.value = error instanceof Error ? error.message : "Request failed";
      }
    }

    async function submitCreate() {
      try {
        await createOnlineAuction(
          tenantStore.selectedTenantId,
          {
            title: createForm.title,
            slug: createForm.slug,
            minimumIncrement: Number(createForm.minimumIncrement),
            countdownSeconds: Number(createForm.countdownSeconds),
            lots: createForm.lots.map((item) => ({ ...item, startingBid: Number(item.startingBid) }))
          },
          authStore.token
        );
        createForm.lots = [emptyLot()];
        toast.success("Online auction created", createForm.title || "Online auction draft created.");
        await reloadData();
      } catch (error) {
        errorMessage.value = error instanceof Error ? error.message : "Request failed";
      }
    }

    async function reviewRegistration(registrationId, decision) {
      if (!selectedAuction.value) {
        return;
      }
      try {
        await reviewOnlineAuctionRegistration(
          tenantStore.selectedTenantId,
          selectedAuction.value.id,
          registrationId,
          {
            kycStatus: reviewForms[registrationId]?.kycStatus ?? "PENDING",
            accountCheckStatus: reviewForms[registrationId]?.accountCheckStatus ?? "PENDING",
            reviewNote: reviewForms[registrationId]?.reviewNote ?? "",
            decision
          },
          authStore.token
        );
        toast.success(decision === "APPROVE" ? "Bidder approved" : "Bidder rejected", "Registration review was saved.");
        await reloadData();
      } catch (error) {
        errorMessage.value = error instanceof Error ? error.message : "Request failed";
      }
    }

    async function changeStatus(action) {
      if (!selectedAuction.value) {
        return;
      }
      try {
        const tenantId = tenantStore.selectedTenantId;
        const auctionId = selectedAuction.value.id;
        if (action === "publish") {
          await publishOnlineAuction(tenantId, auctionId, authStore.token);
          toast.info("Auction published", `${selectedAuction.value.title} is now publicly visible.`);
        }
        if (action === "start") {
          await startOnlineAuction(tenantId, auctionId, authStore.token);
          toast.info("Auction started", `${selectedAuction.value.title} is now live.`);
        }
        if (action === "close") {
          await closeOnlineAuction(tenantId, auctionId, authStore.token);
          toast.info("Auction closed", `${selectedAuction.value.title} was closed.`);
        }
        await reloadData();
      } catch (error) {
        errorMessage.value = error instanceof Error ? error.message : "Request failed";
      }
    }

    function addLot() {
      createForm.lots.push(emptyLot());
    }

    function publicUrlFor(auction) {
      return `${window.location.origin}/online-auctions/${auction.tenantId}/${auction.id}`;
    }

    onMounted(reloadData);

    return {
      addLot,
      auctions,
      changeStatus,
      createForm,
      errorMessage,
      publicUrlFor,
      reviewForms,
      reviewRegistration,
      selectedAuction,
      selectedAuctionId,
      submitCreate
    };
  },
  template
});
