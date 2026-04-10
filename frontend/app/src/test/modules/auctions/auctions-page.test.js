import { flushPromises, mount } from "@vue/test-utils";
import AuctionsView from "../../../modules/auctions/ui/pages/auctions-page";
import { setLocale } from "../../../app/i18n";
import { useAuthStore } from "../../../app/session/state";
import { useTenantStore } from "../../../app/tenant-context/state";
import * as auctionApi from "../../../modules/auctions/infrastructure/api/auction.api";
import { AuctionStatus, AuctionLotStatus } from "../../../modules/auctions/infrastructure/adapters/http-auctions.adapter";

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
        status: AuctionStatus.ANNOUNCED,
        publicAnnouncementDate: "2026-03-18",
        auctionDate: "2026-03-26",
        lots: [
          {
            id: "lot-1",
            lotNumber: 1,
            contractNumber: "PS-5001",
            itemNumber: "I-1",
            description: "Goldring 585",
            estimatedValue: 500,
            outstandingClaim: 300,
            latestBidAmount: 300,
            status: AuctionLotStatus.OPEN,
            hammerPrice: 0,
            authorityTransferDueDate: "2026-04-01",
            authorityTransferStatus: "OPEN"
          }
        ]
      }
    ]);
    vi.spyOn(auctionApi, "fetchSurplusCases").mockResolvedValue([
      {
        id: "surplus-1",
        lotId: "lot-1",
        lotNumber: 1,
        contractNumber: "PS-5001",
        itemNumber: "I-1",
        description: "Goldring 585",
        estimatedValue: 500,
        hammerPrice: 420,
        outstandingClaim: 300,
        surplusAmount: 120,
        authorityTransferDueDate: "2026-04-01",
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
        status: AuctionStatus.ANNOUNCED,
        publicAnnouncementDate: "2026-03-18",
        auctionDate: "2026-03-26",
        lots: [
          {
            id: "lot-1",
            lotNumber: 1,
            contractNumber: "PS-5001",
            itemNumber: "I-1",
            description: "X",
            estimatedValue: 100,
            outstandingClaim: 50,
            latestBidAmount: 0,
            status: AuctionLotStatus.OPEN,
            hammerPrice: 0,
            authorityTransferDueDate: "2026-04-01",
            authorityTransferStatus: "OPEN"
          }
        ]
      }
    ]);
    vi.spyOn(auctionApi, "fetchSurplusCases").mockResolvedValue([]);

    const wrapper = mount(AuctionsView);
    await flushPromises();

    wrapper.vm.selectedAuctionId = "auction-default-001";
    await flushPromises();

    // try to bid
    await wrapper.vm.submitBid("lot-1");
    await flushPromises();

    expect(wrapper.vm.errorMessage).toBe("Bids can only be recorded once the auction is live.");
  });
});
