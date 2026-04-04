import { flushPromises, mount } from "@vue/test-utils";
import PawnTicketsView from "../../../modules/pawn-tickets/ui/pages/pawn-tickets-page";
import { useTenantStore } from "../../../app/tenant-context/state";
import * as pawnTicketApi from "../../../modules/pawn-tickets/infrastructure/api/pawn-ticket.api";

describe("PawnTicketsView", () => {
  let tenantStore;

  beforeEach(() => {
    tenantStore = useTenantStore();
  });

  it("loads pawn tickets from the API layer", async () => {
    tenantStore.selectedTenantId = "tenant-default";
    tenantStore.tenants = [{ id: "tenant-default", displayName: "Default Tenant" }];

    vi.spyOn(pawnTicketApi, "fetchPawnTickets").mockResolvedValue([
      {
        contractNumber: "PS-1001",
        ticketNumber: "PS-1001",
        contractBarcode: "PS-1001",
        termsVersion: "AGB-2026-03",
        customerNumber: "KD-1001",
        customerDisplayName: "Anna Becker",
        createdAt: "2026-03-18T12:00:00Z",
        dueDate: "2099-06-18",
        earliestAuctionDate: "2099-07-18",
        totalLoanValue: 180,
        totalRepaymentAmount: 196.5,
        positionCount: 1
      }
    ]);

    const wrapper = mount(PawnTicketsView);
    await flushPromises();

    expect(pawnTicketApi.fetchPawnTickets).toHaveBeenCalledWith("tenant-default");
    expect(wrapper.text()).toContain("PS-1001");
    expect(wrapper.text()).toContain("AGB-2026-03");
    expect(wrapper.text()).toContain("Anna Becker");
  });
});
