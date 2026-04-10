import { flushPromises, mount } from "@vue/test-utils";
import TenantHomeView from "../../../modules/tenant-dashboard/ui/pages/tenant-dashboard-page";
import { setLocale } from "../../../app/i18n";
import { useTenantStore } from "../../../app/tenant-context/state";
import { useAuthStore } from "../../../app/session/state";
import * as originationApi from "../../../modules/loans/infrastructure/api/origination.api";
import * as customerApi from "../../../modules/customers/infrastructure/api/customer.api";
import * as reportingApi from "../../../modules/tenant-dashboard/infrastructure/api/reporting.api";
import router from "../../../app/router";

function mountView() {
  return mount(TenantHomeView, {
    global: {
      plugins: [router]
    }
  });
}

describe("TenantHomeView - Reporting", () => {
  let authStore;
  let tenantStore;

  beforeEach(() => {
    setLocale("de");
    vi.restoreAllMocks();
    authStore = useAuthStore();
    tenantStore = useTenantStore();
    tenantStore.features = [];
    vi.spyOn(reportingApi, "fetchDashboardOverview").mockResolvedValue({
      rangeStart: "2026-03-05",
      rangeEnd: "2026-03-18",
      generatedAt: "2026-03-18T12:00:00Z",
      finance: {
        cashInflow: 500.8,
        cashOutflow: 440,
        netCashflow: 60.8,
        realizedRevenue: 40.8,
        activeLoanExposure: 440,
        activeTicketCount: 2,
        averageTicketValue: 220
      },
      financeTrend: [
        { date: "2026-03-16", cashInflow: 219.5, cashOutflow: 0, realizedRevenue: 19.5 },
        { date: "2026-03-17", cashInflow: 281.3, cashOutflow: 440, realizedRevenue: 21.3 }
      ],
      inventoryByCategory: [
        { category: "Apple iPhone 14", itemCount: 1, pledgedValue: 260 },
        { category: "Goldring 585", itemCount: 1, pledgedValue: 180 }
      ],
      transactionMix: [
        { type: "EXTEND", transactionCount: 1, totalAmount: 281.3 },
        { type: "REDEEM", transactionCount: 1, totalAmount: 219.5 }
      ]
    });
  });

  it("renders reporting cards in the dashboard", async () => {
    tenantStore.selectedTenantId = "tenant-default";
    tenantStore.tenants = [{ id: "tenant-default", displayName: "Default Tenant" }];
    authStore.token = "token-123";

    vi.spyOn(originationApi, "fetchValuationGuidelines").mockResolvedValue([]);
    vi.spyOn(customerApi, "searchCustomers").mockResolvedValue([]);

    const wrapper = mountView();
    await flushPromises();

    wrapper.vm.reportingOverview = {
      finance: {
        cashInflow: 125000,
        cashOutflow: 4500,
        netCashflow: 120500,
        realizedRevenue: 8200,
        activeLoanExposure: 15600,
        activeTicketCount: 412
      },
      financeTrend: [
        { date: "2026-04-04", cashInflow: 125000, cashOutflow: 4500, realizedRevenue: 8200 }
      ],
      inventoryByCategory: [
        { guidelineId: "g1", category: "Gold 585", itemCount: 120, pledgedValue: 45000 },
        { guidelineId: "g2", category: "Apple iPhone 14", itemCount: 15, pledgedValue: 8500 }
      ],
      transactionMix: [
        { type: "REDEEM", transactionCount: 1, totalAmount: 200 },
        { type: "EXTEND", transactionCount: 1, totalAmount: 25 }
      ],
      recentActivities: [
        { id: "a1", type: "REDEEM", label: "Auslösung", amount: 200, timestamp: "2026-04-04T10:00:00Z" },
        { id: "a2", type: "EXTEND", label: "Verlängerung", amount: 25, timestamp: "2026-04-04T10:30:00Z" }
      ]
    };
    await flushPromises();

    expect(wrapper.text()).toContain("Finanzen und Pfandbestand");
    expect(wrapper.text()).toContain("Apple iPhone 14");
    expect(wrapper.text()).toContain("Verlängerung");
  });
});
