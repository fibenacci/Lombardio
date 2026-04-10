import { flushPromises, mount } from "@vue/test-utils";
import TenantHomeView from "../../../modules/tenant-dashboard/ui/pages/tenant-dashboard-page";
import { setLocale } from "../../../app/i18n";
import { useTenantStore } from "../../../app/tenant-context/state";
import { useAuthStore } from "../../../app/session/state";
import * as originationApi from "../../../modules/loans/infrastructure/api/origination.api";
import * as customerApi from "../../../modules/customers/infrastructure/api/customer.api";
import * as pawnTicketApi from "../../../modules/pawn-tickets/infrastructure/api/pawn-ticket.api";
import * as reportingApi from "../../../modules/tenant-dashboard/infrastructure/api/reporting.api";
import router from "../../../app/router";

function mountView() {
  return mount(TenantHomeView, {
    global: {
      plugins: [router]
    }
  });
}

describe("TenantHomeView - Origination", () => {
  let authStore;
  let tenantStore;

  beforeEach(() => {
    setLocale("de");
    vi.restoreAllMocks();
    authStore = useAuthStore();
    tenantStore = useTenantStore();
    tenantStore.features = [];
    vi.spyOn(reportingApi, "fetchDashboardOverview").mockResolvedValue({
      finance: { cashInflow: 0, cashOutflow: 0, netCashflow: 0, realizedRevenue: 0, activeLoanExposure: 0, activeTicketCount: 0 },
      financeTrend: [],
      inventoryByCategory: [],
      transactionMix: []
    });
    vi.spyOn(originationApi, "fetchValuationGuidelines").mockResolvedValue([
      { id: "guideline-gold-585", label: "Goldring 585", baseLoanValue: 180 }
    ]);
  });

  it("blocks submit when required fields are missing", async () => {
    tenantStore.selectedTenantId = "tenant-default";
    tenantStore.tenants = [{ id: "tenant-default", displayName: "Default Tenant" }];
    authStore.token = "token-123";

    vi.spyOn(customerApi, "searchCustomers").mockResolvedValue([]);
    const createLoanSpy = vi.spyOn(originationApi, "createLoan").mockResolvedValue({});

    const wrapper = mountView();
    await flushPromises();

    const submitButton = wrapper.findAll("button").find((button) => button.text().includes("Beleihung abschließen"));
    expect(submitButton.attributes("disabled")).toBeDefined();
    expect(createLoanSpy).not.toHaveBeenCalled();
  });

  it("creates a loan and shows the pawn ticket preview", async () => {
    tenantStore.selectedTenantId = "tenant-default";
    tenantStore.tenants = [{ id: "tenant-default", displayName: "Default Tenant" }];
    authStore.token = "token-123";

    vi.spyOn(customerApi, "searchCustomers").mockResolvedValue([
      { id: "customer-berlin-1", displayName: "Anna Becker", kycApproved: true }
    ]);
    vi.spyOn(pawnTicketApi, "fetchPawnTicketQuote").mockResolvedValue({
      totalRepaymentAmount: 219.5,
      positions: []
    });
    vi.spyOn(pawnTicketApi, "fetchPawnTicketDocument").mockResolvedValue(new Blob(["pdf"], { type: "application/pdf" }));
    const openSpy = vi.fn();
    window.open = openSpy;
    URL.createObjectURL = vi.fn().mockReturnValue("blob:ticket");

    vi.spyOn(originationApi, "createLoan").mockResolvedValue({
      id: "loan-1",
      customer: { displayName: "Anna Becker" },
      pawnTickets: [
        { 
          ticketNumber: "PS-1001",
          contractNumber: "PS-1001", 
          totalRepaymentAmount: 219.5, 
          positions: [{ itemNumber: "PS-1001-01" }] 
        }
      ]
    });

    const wrapper = mountView();
    await flushPromises();

    wrapper.vm.customers = [{ id: "customer-berlin-1", displayName: "Anna Becker", kycApproved: true, amlOriginationAllowed: true }];
    wrapper.vm.handleCustomerSelection({ value: { value: "customer-berlin-1", label: "Anna" } });
    wrapper.vm.positions = [{ 
      ticketGroup: 1, 
      label: "Goldring 585", 
      description: "Gelbgold 14 Karat", 
      guidelineId: "guideline-gold-585", 
      pledgedValue: 200 
    }];
    
    await wrapper.vm.submitLoan();
    await flushPromises();

    expect(originationApi.createLoan).toHaveBeenCalled();
    expect(wrapper.text()).toContain("PS-1001");
    expect(wrapper.text()).toContain("Anna Becker");

    const pdfButton = wrapper.findAll("button").find((button) => button.text().includes("Pfandschein als PDF"));
    await pdfButton.trigger("click");
    await flushPromises();

    expect(openSpy).toHaveBeenCalled();
  });

  it("shows structured field errors from the backend", async () => {
    tenantStore.selectedTenantId = "tenant-default";
    tenantStore.tenants = [{ id: "tenant-default", displayName: "Default Tenant" }];
    authStore.token = "token-123";

    const validationError = new Error("Validation failed");
    validationError.fieldErrors = [{ field: "positions[0].description", message: "must not be blank" }];
    vi.spyOn(originationApi, "createLoan").mockRejectedValue(validationError);

    const wrapper = mountView();
    await flushPromises();

    wrapper.vm.customers = [{ id: "customer-berlin-1", displayName: "Max", kycApproved: true, amlOriginationAllowed: true }];
    wrapper.vm.handleCustomerSelection({ value: { value: "customer-berlin-1" } });
    wrapper.vm.positions = [{ 
      ticketGroup: 1, 
      label: "Goldring 585", 
      description: "Gelbgold 14 Karat", 
      guidelineId: "guideline-gold-585", 
      pledgedValue: 200 
    }];
    await flushPromises();
    
    const submitButton = wrapper.findAll("button").find((button) => button.text().includes("Beleihung abschließen"));
    await submitButton.trigger("click");
    await flushPromises();

    expect(wrapper.text()).toContain("positions 0 description");
    expect(wrapper.text()).toContain("must not be blank");
  });

  it("shows a separate quote preview per pawn-ticket group", async () => {
    tenantStore.selectedTenantId = "tenant-default";
    tenantStore.tenants = [{ id: "tenant-default", displayName: "Default Tenant" }];
    authStore.token = "token-123";

    vi.spyOn(pawnTicketApi, "fetchPawnTicketQuote")
      .mockResolvedValue({ contractNumber: "PS-1", legalText: "Kostenmodell", positions: [] });

    const wrapper = mountView();
    await flushPromises();

    wrapper.vm.positions = [
      { ticketGroup: 1, label: "Gold", description: "D1", guidelineId: "g1", pledgedValue: 200 },
      { ticketGroup: 2, label: "Silber", description: "D2", guidelineId: "g2", pledgedValue: 100 }
    ];
    await flushPromises();

    expect(pawnTicketApi.fetchPawnTicketQuote).toHaveBeenCalled();
    expect(wrapper.text()).toContain("Pfandschein 1");
    expect(wrapper.text()).toContain("Pfandschein 2");
  });
});
