import { flushPromises, mount } from "@vue/test-utils";
import TenantsView from ".";
import { useAuthStore } from "../../stores/auth";
import { useTenantStore } from "../../stores/tenant";
import * as platformApi from "../../services/api/platform";

describe("TenantsView", () => {
  let authStore;
  let tenantStore;

  beforeEach(() => {
    authStore = useAuthStore();
    tenantStore = useTenantStore();
  });

  it("loads tenants and tenant features from the platform API", async () => {
    authStore.token = "platform-token";
    authStore.user = {
      id: "user-platform-admin",
      tenantId: "tenant-platform",
      permissions: ["platform.tenants.read", "platform.tenants.write"]
    };
    tenantStore.tenants = [];
    tenantStore.selectedTenantId = "";
    tenantStore.features = [];

    vi.spyOn(platformApi, "fetchTenants").mockResolvedValue([
      {
        id: "tenant-default",
        key: "default",
        displayName: "Default Tenant",
        status: "ACTIVE"
      }
    ]);
    vi.spyOn(platformApi, "fetchTenantFeatures").mockResolvedValue([
      {
        tenantId: "tenant-default",
        featureKey: "identity-access",
        enabled: true
      }
    ]);

    const wrapper = mount(TenantsView);
    await flushPromises();

    expect(platformApi.fetchTenants).toHaveBeenCalledWith("platform-token");
    expect(platformApi.fetchTenantFeatures).toHaveBeenCalledWith("tenant-default", "platform-token");
    expect(wrapper.text()).toContain("Default Tenant");
    expect(wrapper.text()).toContain("Identity Access");
  });
});
