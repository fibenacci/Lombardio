import { flushPromises, mount } from "@vue/test-utils";
import CashdeskView from ".";
import { setLocale } from "../../i18n";
import { authStore } from "../../stores/auth";
import { tenantStore } from "../../stores/tenant";
import * as pawnTicketApi from "../../services/api/pawnTicket";

describe("CashdeskView", () => {
  beforeEach(() => {
    setLocale("de");
  });

  it("loads tickets and calculates a redemption settlement", async () => {
    authStore.token = "token-123";
    tenantStore.selectedTenantId = "tenant-default";
    tenantStore.tenants = [{ id: "tenant-default", displayName: "Default Tenant" }];

    vi.spyOn(pawnTicketApi, "fetchPawnTickets").mockResolvedValue([
      {
        ticketNumber: "PS-1001",
        customerNumber: "KD-1001",
        customerDisplayName: "Anna Becker",
        createdAt: "2026-03-18T12:00:00Z",
        dueDate: "2026-06-18",
        earliestAuctionDate: "2026-07-18",
        totalLoanValue: 180,
        totalRepaymentAmount: 196.5,
        positionCount: 1
      }
    ]);
    vi.spyOn(pawnTicketApi, "fetchCashTransactions").mockResolvedValue([]);
    vi.spyOn(pawnTicketApi, "redeemPawnTicket").mockResolvedValue({
      outstandingLoanAmount: 180,
      interestAmount: 5.4,
      operatingFeeAmount: 11.1,
      totalDueAmount: 196.5,
      legalText: "Kostenmodell gemaess PfandlV."
    });

    const wrapper = mount(CashdeskView);
    await flushPromises();

    await wrapper.find("button").trigger("click");
    await flushPromises();

    expect(pawnTicketApi.fetchPawnTickets).toHaveBeenCalledWith("tenant-default", "token-123");
    expect(pawnTicketApi.redeemPawnTicket).toHaveBeenCalled();
    expect(wrapper.text()).toContain("196.5 EUR");
  });

  it("persists a cash transaction into the journal", async () => {
    authStore.token = "token-123";
    tenantStore.selectedTenantId = "tenant-default";
    tenantStore.tenants = [{ id: "tenant-default", displayName: "Default Tenant" }];

    const fetchTransactionsSpy = vi.spyOn(pawnTicketApi, "fetchCashTransactions")
      .mockResolvedValueOnce([])
      .mockResolvedValueOnce([
        {
          id: "cash-1",
          ticketNumber: "PS-1001",
          customerDisplayName: "Anna Becker",
          type: "REDEEM",
          totalAmount: 196.5,
          createdAt: "2026-03-18T12:00:00Z"
        }
      ]);
    vi.spyOn(pawnTicketApi, "fetchPawnTickets").mockResolvedValue([
      {
        ticketNumber: "PS-1001",
        customerNumber: "KD-1001",
        customerDisplayName: "Anna Becker",
        createdAt: "2026-03-18T12:00:00Z",
        dueDate: "2026-06-18",
        earliestAuctionDate: "2026-07-18",
        totalLoanValue: 180,
        totalRepaymentAmount: 196.5,
        positionCount: 1
      }
    ]);
    vi.spyOn(pawnTicketApi, "redeemPawnTicket").mockResolvedValue({
      outstandingLoanAmount: 180,
      interestAmount: 5.4,
      operatingFeeAmount: 11.1,
      totalDueAmount: 196.5,
      legalText: "Kostenmodell gemaess PfandlV."
    });
    const executeSpy = vi.spyOn(pawnTicketApi, "executeCashTransaction").mockResolvedValue({
      id: "cash-1",
      ticketNumber: "PS-1001"
    });

    const wrapper = mount(CashdeskView);
    await flushPromises();

    await wrapper.find("button").trigger("click");
    await flushPromises();

    const buttons = wrapper.findAll("button");
    await buttons[1].trigger("click");
    await flushPromises();

    expect(executeSpy).toHaveBeenCalledWith(
      expect.objectContaining({
        tenantId: "tenant-default",
        ticketNumber: "PS-1001",
        type: "REDEEM"
      }),
      "token-123"
    );
    expect(fetchTransactionsSpy).toHaveBeenCalledTimes(2);
    expect(wrapper.text()).toContain("Kassenjournal");
    expect(wrapper.text()).toContain("Anna Becker");
  });
});
