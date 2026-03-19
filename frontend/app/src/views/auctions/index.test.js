import { flushPromises, mount } from "@vue/test-utils";
import AuctionsView from ".";
import { authStore } from "../../stores/auth";
import { tenantStore } from "../../stores/tenant";
import * as auctionApi from "../../services/api/auction";
import router from "../../router";

describe("AuctionsView", () => {
  it("loads auctions and surplus cases from the API layer", async () => {
    authStore.token = "token-123";
    tenantStore.selectedTenantId = "tenant-default";
    tenantStore.tenants = [{ id: "tenant-default", displayName: "Default Tenant" }];

    vi.spyOn(auctionApi, "fetchAuctions").mockResolvedValue([
      {
        id: "auction-1",
        title: "Fruehjahrsauktion",
        location: "Berlin",
        status: "ANNOUNCED",
        publicAnnouncementDate: "2026-03-18",
        auctionDate: "2026-03-26",
        lots: [
          {
            id: "lot-1",
            lotNumber: 1,
            contractNumber: "PS-5001",
            description: "Goldring 585",
            latestBidAmount: 300
          }
        ]
      }
    ]);
    vi.spyOn(auctionApi, "fetchSurplusCases").mockResolvedValue([
      {
        lotId: "lot-1",
        lotNumber: 1,
        contractNumber: "PS-5001",
        surplusAmount: 120,
        authorityTransferStatus: "OPEN"
      }
    ]);

    await router.push("/app/auctions");
    await router.isReady();

    const wrapper = mount(AuctionsView, {
      global: {
        plugins: [router]
      }
    });
    await flushPromises();

    expect(wrapper.text()).toContain("Fruehjahrsauktion");
    expect(wrapper.text()).toContain("PS-5001");
    expect(wrapper.text()).toContain("OPEN");
  });
});
