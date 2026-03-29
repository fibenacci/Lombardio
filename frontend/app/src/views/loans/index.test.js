import { flushPromises, mount } from "@vue/test-utils";
import LoansView from ".";
import router from "../../router";
import { useAuthStore } from "../../stores/auth";
import { useTenantStore } from "../../stores/tenant";
import * as originationApi from "../../services/api/origination";

describe("LoansView", () => {
  let authStore;
  let tenantStore;

  beforeEach(() => {
    authStore = useAuthStore();
    tenantStore = useTenantStore();
  });

  it("loads the chronological pledge register from the API layer", async () => {
    authStore.token = "token-123";
    tenantStore.selectedTenantId = "tenant-default";
    tenantStore.tenants = [{ id: "tenant-default", displayName: "Default Tenant" }];

    vi.spyOn(originationApi, "fetchLoans").mockResolvedValue([
      {
        id: "loan-1",
        customer: {
          id: "customer-1",
          customerNumber: "KD-1001",
          displayName: "Anna Becker"
        },
        pledgeRecord: {
          recordedAt: "2026-03-18T12:00:00Z",
          checkedDocumentType: "PERSONALAUSWEIS",
          powerOfAttorneyRequired: false,
          retentionUntil: "2030-03-18"
        },
        pawnTickets: [
          {
            ticketNumber: "PS-1001",
            totalLoanValue: 200
          }
        ]
      }
    ]);

    await router.push("/app/loans");
    await router.isReady();

    const wrapper = mount(LoansView, {
      global: {
        plugins: [router]
      }
    });
    await flushPromises();

    expect(originationApi.fetchLoans).toHaveBeenCalledWith("tenant-default", "token-123");
    expect(wrapper.text()).toContain("Anna Becker");
    expect(wrapper.text()).toContain("PS-1001");
    expect(wrapper.text()).toContain("2030-03-18");
  });
});
