import { computed, reactive, ref } from "vue";
import { getRequestErrorMessage } from "../../../../shared/kernel/errors/request-error";
import { useAppToast } from "../../../../shared/ui/composables/use-app-toast";
import { createHttpOnlineAuctionsAdapter } from "../../infrastructure/adapters/http-online-auctions.adapter";

type OnlineAuctionLotDraft = {
  title: string;
  description: string;
  startingBid: string;
};

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

function emptyLot() {
  return { title: "", description: "", startingBid: "0.00" } satisfies OnlineAuctionLotDraft;
}

function hasText(value: string) {
  return String(value).trim().length > 0;
}

function isPositiveAmount(value: string | number) {
  return Number(value) > 0;
}

function isValidCountdown(value: string | number) {
  return Number.isInteger(Number(value)) && Number(value) >= 30;
}

function hasValidOnlineAuctionLot(lot: OnlineAuctionLotDraft) {
  return hasText(lot.title) && hasText(lot.description) && isPositiveAmount(lot.startingBid);
}

export function useOnlineAuctionsPage({
  t,
  tenantStore
}: {
  t: (key: string, params?: Record<string, unknown>) => string;
  tenantStore: { selectedTenantId: string };
}) {
  const adapter = createHttpOnlineAuctionsAdapter();
  const toast = useAppToast();
  const auctions = ref<OnlineAuction[]>([]);
  const selectedAuctionId = ref("");
  const errorMessage = ref("");
  const createForm = reactive({
    title: "",
    slug: "",
    minimumIncrement: "10.00",
    countdownSeconds: "180",
    lots: [emptyLot()]
  });
  const reviewForms = reactive<Record<string, { kycStatus: string; accountCheckStatus: string; reviewNote: string }>>(
    {}
  );

  const selectedAuction = computed(
    () => auctions.value.find((item) => item.id === selectedAuctionId.value) ?? null
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

  function getAuctionStatusLabel(status: string) {
    return t(`onlineAuctions.status.${status}`);
  }

  function getApprovalStatusLabel(status: string) {
    return t(`onlineAuctions.registrationStatus.approval.${status}`);
  }

  function getRegistrationSummary(registration: OnlineAuctionRegistration) {
    return t("onlineAuctions.registrationSummary", {
      account: t(`onlineAuctions.registrationStatus.account.${registration.accountCheckStatus}`),
      kyc: t(`onlineAuctions.registrationStatus.kyc.${registration.kycStatus}`)
    });
  }

  function canCreateOnlineAuction() {
    const hasHeader = hasText(createForm.title) && hasText(createForm.slug);
    const hasIncrement = isPositiveAmount(createForm.minimumIncrement);
    const hasCountdown = isValidCountdown(createForm.countdownSeconds);
    const hasLots = createForm.lots.length > 0;
    const hasValidLots = createForm.lots.every(hasValidOnlineAuctionLot);

    return hasHeader && hasIncrement && hasCountdown && hasLots && hasValidLots;
  }

  async function loadData() {
    if (!tenantStore.selectedTenantId) {
      auctions.value = [];
      return;
    }
    auctions.value = await adapter.fetchOnlineAuctions(tenantStore.selectedTenantId);
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
      errorMessage.value = getRequestErrorMessage(error, t("common.requestFailed"));
    }
  }

  async function submitCreate() {
    try {
      errorMessage.value = "";
      if (!canCreateOnlineAuction()) {
        errorMessage.value = t("onlineAuctions.messages.invalidCreateForm");
        return;
      }
      await adapter.createOnlineAuction(
        tenantStore.selectedTenantId,
        {
          title: createForm.title,
          slug: createForm.slug,
          minimumIncrement: Number(createForm.minimumIncrement),
          countdownSeconds: Number(createForm.countdownSeconds),
          lots: createForm.lots.map((item) => ({ ...item, startingBid: Number(item.startingBid) }))
        },
      );
      createForm.lots = [emptyLot()];
      toast.success(
        t("onlineAuctions.messages.createdTitle"),
        createForm.title || t("onlineAuctions.messages.createdFallback")
      );
      await reloadData();
    } catch (error) {
      errorMessage.value = getRequestErrorMessage(error, t("common.requestFailed"));
    }
  }

  async function reviewRegistration(registrationId: string, decision: string) {
    if (!selectedAuction.value) {
      return;
    }
    try {
      await adapter.reviewOnlineAuctionRegistration(
        tenantStore.selectedTenantId,
        selectedAuction.value.id,
        registrationId,
        {
          kycStatus: reviewForms[registrationId]?.kycStatus ?? "PENDING",
          accountCheckStatus: reviewForms[registrationId]?.accountCheckStatus ?? "PENDING",
          reviewNote: reviewForms[registrationId]?.reviewNote ?? "",
          decision
        },
      );
      toast.success(
        decision === "APPROVE"
          ? t("onlineAuctions.messages.bidderApprovedTitle")
          : t("onlineAuctions.messages.bidderRejectedTitle"),
        t("onlineAuctions.messages.registrationSavedToast")
      );
      await reloadData();
    } catch (error) {
      errorMessage.value = getRequestErrorMessage(error, t("common.requestFailed"));
    }
  }

  async function changeStatus(action: string) {
    if (!selectedAuction.value) {
      return;
    }
    try {
      const tenantId = tenantStore.selectedTenantId;
      const auctionId = selectedAuction.value.id;
      if (action === "publish") {
        await adapter.publishOnlineAuction(tenantId, auctionId);
        toast.info(t("onlineAuctions.messages.publishedTitle"), t("onlineAuctions.messages.publishedToast", { title: selectedAuction.value.title }));
      }
      if (action === "start") {
        await adapter.startOnlineAuction(tenantId, auctionId);
        toast.info(t("onlineAuctions.messages.startedTitle"), t("onlineAuctions.messages.startedToast", { title: selectedAuction.value.title }));
      }
      if (action === "close") {
        await adapter.closeOnlineAuction(tenantId, auctionId);
        toast.info(t("onlineAuctions.messages.closedTitle"), t("onlineAuctions.messages.closedToast", { title: selectedAuction.value.title }));
      }
      await reloadData();
    } catch (error) {
      errorMessage.value = getRequestErrorMessage(error, t("common.requestFailed"));
    }
  }

  function addLot() {
    createForm.lots.push(emptyLot());
  }

  function publicUrlFor(auction: { tenantId: string; id: string }) {
    return `${window.location.origin}/online-auctions/${auction.tenantId}/${auction.id}`;
  }

  return {
    addLot,
    auctions,
    canCreateOnlineAuction,
    changeStatus,
    createForm,
    errorMessage,
    getApprovalStatusLabel,
    getAuctionStatusLabel,
    getRegistrationSummary,
    loadData,
    publicUrlFor,
    registrationAccountOptions,
    registrationKycOptions,
    reloadData,
    reviewForms,
    reviewRegistration,
    selectedAuction,
    selectedAuctionId,
    submitCreate
  };
}
