import { computed, defineComponent, onMounted, reactive, ref } from "vue";
import { useAuthStore } from "../../stores/auth";
import { useTenantStore } from "../../stores/tenant";
import {
  closeOnlineAuction,
  createOnlineAuction,
  fetchOnlineAuctions,
  publishOnlineAuction,
  reviewOnlineAuctionRegistration,
  startOnlineAuction
} from "../../services/api/onlineAuction";
import { useAppToast } from "../../composables/use-app-toast";
import { useI18n } from "../../i18n";
import template from "./template.html?raw";
import "./styles.scss";

function emptyLot() {
  return { title: "", description: "", startingBid: "0.00" };
}

export default defineComponent({
  name: "OnlineAuctionsView",
  setup() {
    const authStore = useAuthStore();
    const tenantStore = useTenantStore();
    const toast = useAppToast();
    const { t } = useI18n();
    const auctions = ref([]);
    const selectedAuctionId = ref("");
    const errorMessage = ref("");
    const createForm = reactive({
      title: "",
      slug: "",
      minimumIncrement: "10.00",
      countdownSeconds: "180",
      lots: [emptyLot()]
    });
    const reviewForms = reactive({});

    const selectedAuction = computed(() =>
      auctions.value.find((item) => item.id === selectedAuctionId.value) ?? null
    );
    const registrationKycOptions = computed(() => [
      { label: t("onlineAuctions.registrationStatus.kyc.PENDING"), value: "PENDING" },
      { label: t("onlineAuctions.registrationStatus.kyc.PASSED"), value: "PASSED" },
      { label: t("onlineAuctions.registrationStatus.kyc.FAILED"), value: "FAILED" }
    ]);
    const registrationAccountOptions = computed(() => [
      { label: t("onlineAuctions.registrationStatus.account.PENDING"), value: "PENDING" },
      { label: t("onlineAuctions.registrationStatus.account.PASSED"), value: "PASSED" },
      { label: t("onlineAuctions.registrationStatus.account.FAILED"), value: "FAILED" }
    ]);

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
        errorMessage.value = error instanceof Error ? error.message : t("common.requestFailed");
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
        toast.success(
          t("onlineAuctions.messages.createdTitle"),
          createForm.title || t("onlineAuctions.messages.createdFallback")
        );
        await reloadData();
      } catch (error) {
        errorMessage.value = error instanceof Error ? error.message : t("common.requestFailed");
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
        toast.success(
          decision === "APPROVE"
            ? t("onlineAuctions.messages.bidderApprovedTitle")
            : t("onlineAuctions.messages.bidderRejectedTitle"),
          t("onlineAuctions.messages.registrationSavedToast")
        );
        await reloadData();
      } catch (error) {
        errorMessage.value = error instanceof Error ? error.message : t("common.requestFailed");
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
          toast.info(t("onlineAuctions.messages.publishedTitle"), t("onlineAuctions.messages.publishedToast", { title: selectedAuction.value.title }));
        }
        if (action === "start") {
          await startOnlineAuction(tenantId, auctionId, authStore.token);
          toast.info(t("onlineAuctions.messages.startedTitle"), t("onlineAuctions.messages.startedToast", { title: selectedAuction.value.title }));
        }
        if (action === "close") {
          await closeOnlineAuction(tenantId, auctionId, authStore.token);
          toast.info(t("onlineAuctions.messages.closedTitle"), t("onlineAuctions.messages.closedToast", { title: selectedAuction.value.title }));
        }
        await reloadData();
      } catch (error) {
        errorMessage.value = error instanceof Error ? error.message : t("common.requestFailed");
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
      registrationAccountOptions,
      registrationKycOptions,
      reviewForms,
      reviewRegistration,
      selectedAuction,
      selectedAuctionId,
      submitCreate,
      t
    };
  },
  template
});
