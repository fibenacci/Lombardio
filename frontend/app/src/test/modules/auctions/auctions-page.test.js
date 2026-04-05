import { flushPromises, mount } from "@vue/test-utils";
import AuctionsView from "../../../modules/auctions/ui/pages/auctions-page";
import { setLocale } from "../../../app/i18n";
import { useAuthStore } from "../../../app/session/state";
import { useTenantStore } from "../../../app/tenant-context/state";
import * as auctionApi from "../../../modules/auctions/infrastructure/api/auction.api";

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
  }, 10000);

  it("blocks auction creation with incomplete lot data before the request is sent", async () => {
    authStore.token = "token-123";
    tenantStore.selectedTenantId = "tenant-default";
    tenantStore.tenants = [{ id: "tenant-default", displayName: "Default Tenant" }];

    vi.spyOn(auctionApi, "fetchAuctions").mockResolvedValue([]);
    vi.spyOn(auctionApi, "fetchSurplusCases").mockResolvedValue([]);
    const createAuctionSpy = vi.spyOn(auctionApi, "createAuction").mockResolvedValue({});

    const wrapper = mount(AuctionsView);
    await flushPromises();

    wrapper.vm.createForm.title = "Frühjahrsauktion";
    wrapper.vm.createForm.location = "Berlin";
    wrapper.vm.createForm.lots[0].contractNumber = "PS-5001";
    wrapper.vm.createForm.lots[0].itemNumber = "";
    wrapper.vm.createForm.lots[0].description = "Goldring 585";
    wrapper.vm.createForm.lots[0].estimatedValue = "300.00";
    wrapper.vm.createForm.lots[0].outstandingClaim = "200.00";

    await wrapper.vm.submitCreate();
    await flushPromises();

    expect(createAuctionSpy).not.toHaveBeenCalled();
    expect(wrapper.vm.errorMessage).toBe("Please complete title, location and all lots with valid values.");
  });

  it("blocks bids for non-live auctions before the request is sent", async () => {
    authStore.token = "token-123";
    tenantStore.selectedTenantId = "tenant-default";
    tenantStore.tenants = [{ id: "tenant-default", displayName: "Default Tenant" }];

    vi.spyOn(auctionApi, "fetchAuctions").mockResolvedValue([
      {
        id: "auction-default-001",
        title: "Spring auction",
        location: "Berlin",
        status: "ANNOUNCED",
        publicAnnouncementDate: "2026-03-18",
        auctionDate: "2026-03-26",
        lots: [
          {
            id: "auction-lot-default-001-03",
            lotNumber: 3,
            contractNumber: "PS-5003",
            description: "Gold ring",
            latestBidAmount: 300,
            status: "PENDING"
          }
        ]
      }
    ]);
    vi.spyOn(auctionApi, "fetchSurplusCases").mockResolvedValue([]);
    const placeBidSpy = vi.spyOn(auctionApi, "placeAuctionBid").mockResolvedValue({});

    const wrapper = mount(AuctionsView);
    await flushPromises();

    wrapper.vm.bidForms["auction-lot-default-001-03"].bidderDisplayName = "Bidder 1";
    wrapper.vm.bidForms["auction-lot-default-001-03"].amount = "350";

    await wrapper.vm.submitBid("auction-lot-default-001-03");
    await flushPromises();

    expect(placeBidSpy).not.toHaveBeenCalled();
    expect(wrapper.vm.errorMessage).toBe("Bids can only be recorded once the auction is live.");
  });
});
