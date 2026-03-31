import { flushPromises, mount } from "@vue/test-utils";
import AuctionsView from ".";
import { setLocale } from "../../i18n";
import { useAuthStore } from "../../stores/auth";
import { useTenantStore } from "../../stores/tenant";
import * as auctionApi from "../../services/api/auction";

describe("AuctionsView", () => {
  let authStore;
  let tenantStore;

  beforeEach(() => {
    setLocale("en");
    authStore = useAuthStore();
    tenantStore = useTenantStore();
  });

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

    const wrapper = mount(AuctionsView);
    await flushPromises();

    expect(wrapper.text()).toContain("Fruehjahrsauktion");
    expect(wrapper.text()).toContain("PS-5001");
    expect(wrapper.text()).toContain("OPEN");
  });
});
