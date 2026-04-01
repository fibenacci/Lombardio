import { flushPromises, mount } from "@vue/test-utils";
import CustomersView from "../../../modules/customers/ui/pages/customers-page";
import { setLocale } from "../../../app/i18n";
import { useAuthStore } from "../../../app/session/state";
import { useTenantStore } from "../../../app/tenant-context/state";
import * as customerApi from "../../../modules/customers/infrastructure/api/customer.api";
import * as amlApi from "../../../modules/customers/infrastructure/api/aml.api";
import router from "../../../app/router";

describe("CustomersView", () => {
  let authStore;
  let tenantStore;

  beforeEach(() => {
    setLocale("en");
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
    expect(wrapper.text()).toContain("Approved");
    expect(wrapper.text()).toContain("Clear");
    expect(wrapper.text()).toContain("Details");
  });
});
