import { flushPromises, mount } from "@vue/test-utils";
import OnlineAuctionsView from ".";
import { setLocale } from "../../i18n";
import { useAuthStore } from "../../stores/auth";
import { useTenantStore } from "../../stores/tenant";
import * as onlineAuctionApi from "../../services/api/onlineAuction";
import router from "../../router";

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
        status: "LIVE",
        minimumIncrement: 10,
        countdownSeconds: 180,
        lots: [
          { id: "lot-1", lotNumber: 1, title: "Goldring", startingBid: 100, currentBid: 150, highestBidderAlias: "P1001" }
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
});
