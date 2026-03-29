import { flushPromises, mount } from "@vue/test-utils";
import CustomersView from ".";
import { useAuthStore } from "../../stores/auth";
import { useTenantStore } from "../../stores/tenant";
import * as customerApi from "../../services/api/customer";
import * as amlApi from "../../services/api/aml";
import router from "../../router";

describe("CustomersView", () => {
  let authStore;
  let tenantStore;

  beforeEach(() => {
    authStore = useAuthStore();
    tenantStore = useTenantStore();
  });

  it("loads customers from the API layer", async () => {
    authStore.token = "token-123";
    tenantStore.selectedTenantId = "tenant-default";
    tenantStore.tenants = [{ id: "tenant-default", displayName: "Default Tenant" }];

    vi.spyOn(customerApi, "searchCustomers").mockResolvedValue([
      {
        id: "customer-1",
        customerNumber: "KD-1001",
        displayName: "Anna Becker",
        phone: "+49 170 111111",
        kycStatus: "APPROVED",
        kycApproved: true,
        street: "Hauptstrasse 1",
        postalCode: "10115",
        city: "Berlin"
      }
    ]);
    vi.spyOn(amlApi, "fetchAmlStatus").mockResolvedValue({
      customerId: "customer-1",
      status: "CLEAR",
      riskLevel: "LOW",
      suspiciousActivityReported: false,
      goamlReference: null,
      originationAllowed: true,
      decisionReason: "AML review cleared for origination"
    });

    await router.push("/app/customers");
    await router.isReady();

    const wrapper = mount(CustomersView, {
      global: {
        plugins: [router]
      }
    });
    await flushPromises();

    expect(customerApi.searchCustomers).toHaveBeenCalledWith("tenant-default", "", "token-123");
    expect(wrapper.text()).toContain("Anna Becker");
    expect(wrapper.text()).toContain("APPROVED");
    expect(wrapper.text()).toContain("CLEAR");
    expect(wrapper.text()).toContain("Details");
  });
});
