import { flushPromises, mount } from "@vue/test-utils";
import OnlineAuctionsView from "../../../modules/online-auctions/ui/pages/online-auctions-page";
import { setLocale } from "../../../app/i18n";
import { useAuthStore } from "../../../app/session/state";
import { useTenantStore } from "../../../app/tenant-context/state";
import * as onlineAuctionApi from "../../../modules/online-auctions/infrastructure/api/online-auction.api";
import router from "../../../app/router";
import { OnlineAuctionStatus } from "../../../modules/online-auctions/infrastructure/adapters/http-online-auctions.adapter";

describe("OnlineAuctionsView", () => {
  let authStore;
  let tenantStore;

  beforeEach(() => {
    setLocale("en");
    authStore = useAuthStore();
    tenantStore = useTenantStore();
  });

  it("loads online auctions from the API layer", async () => {
    authStore.token = "token-123";
    tenantStore.selectedTenantId = "tenant-default";
    tenantStore.tenants = [{ id: "tenant-default", displayName: "Default Tenant" }];

    vi.spyOn(onlineAuctionApi, "fetchOnlineAuctions").mockResolvedValue([
      {
        id: "oa-1",
        tenantId: "tenant-default",
        title: "Live Gold Auction",
        slug: "live-gold-auction",
        status: OnlineAuctionStatus.LIVE,
        minimumIncrement: 10,
        countdownSeconds: 180,
        channelName: "ch-1",
        lots: [
          { 
            id: "lot-1", 
            lotNumber: 1, 
            title: "Goldring", 
            description: "D", 
            startingBid: 100, 
            currentBid: 150, 
            highestBidderAlias: "P1001",
            winnerBidderId: null,
            hammerPrice: null,
            status: "OPEN"
          }
        ],
        registrations: []
      }
    ]);

    await router.push("/app/online-auctions");
    await router.isReady();

    const wrapper = mount(OnlineAuctionsView, {
      global: { plugins: [router] }
    });
    await flushPromises();

    expect(wrapper.text()).toContain("Live Gold Auction");
    expect(wrapper.text()).toContain("P1001");
  });

  it("blocks online auction creation with incomplete lot data before the request is sent", async () => {
    authStore.token = "token-123";
    tenantStore.selectedTenantId = "tenant-default";
    tenantStore.tenants = [{ id: "tenant-default", displayName: "Default Tenant" }];

    vi.spyOn(onlineAuctionApi, "fetchOnlineAuctions").mockResolvedValue([]);
    const createOnlineAuctionSpy = vi.spyOn(onlineAuctionApi, "createOnlineAuction").mockResolvedValue({});

    await router.push("/app/online-auctions");
    await router.isReady();

    const wrapper = mount(OnlineAuctionsView, {
      global: { plugins: [router] }
    });
    await flushPromises();

    wrapper.vm.createForm.title = "Live Gold Auction";
    wrapper.vm.createForm.slug = "live-gold-auction";
    wrapper.vm.createForm.minimumIncrement = "10.00";
    wrapper.vm.createForm.countdownSeconds = "180";
    wrapper.vm.createForm.lots[0].title = "Gold ring";
    wrapper.vm.createForm.lots[0].description = "";
    wrapper.vm.createForm.lots[0].startingBid = "100.00";

    await wrapper.vm.submitCreate();
    await flushPromises();

    expect(createOnlineAuctionSpy).not.toHaveBeenCalled();
    expect(wrapper.vm.errorMessage).toBe("Please complete title, slug, minimum increment, countdown and all lots with valid values.");
  });
});
