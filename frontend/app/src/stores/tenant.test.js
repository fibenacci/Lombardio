import { useTenantStore } from "./tenant";
import { useAuthStore } from "./auth";
import * as platformApi from "../services/api/platform";

describe("tenantStore", () => {
  let tenantStore;
  let authStore;

  beforeEach(() => {
    tenantStore = useTenantStore();
    authStore = useAuthStore();
  });

  it("loads tenant features for tenant-scoped users", async () => {
    authStore.user = {
      id: "user-admin",
      tenantId: "tenant-default",
      permissions: ["customers.read"]
    };
    authStore.token = "token-123";

    const fetchFeaturesSpy = vi.spyOn(platformApi, "fetchTenantFeatures").mockResolvedValue([
      { tenantId: "tenant-default", featureKey: "kyc-document-ocr", enabled: true }
    ]);

    await tenantStore.initialize();

    expect(fetchFeaturesSpy).toHaveBeenCalledWith("tenant-default", "token-123");
    expect(tenantStore.selectedTenantId).toBe("tenant-default");
    expect(tenantStore.features).toEqual([
      { tenantId: "tenant-default", featureKey: "kyc-document-ocr", enabled: true }
    ]);
  });
});
