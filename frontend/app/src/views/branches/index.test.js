import { flushPromises, mount } from "@vue/test-utils";
import BranchesView from ".";
import { authStore } from "../../stores/auth";
import { tenantStore } from "../../stores/tenant";
import * as accessApi from "../../services/api/access";

describe("BranchesView", () => {
  it("loads branches from the API layer", async () => {
    authStore.token = "token-123";
    authStore.user = {
      id: "user-admin",
      tenantId: "tenant-default",
      permissions: ["branches.read", "branches.write"]
    };
    tenantStore.selectedTenantId = "tenant-default";

    vi.spyOn(accessApi, "fetchBranches").mockResolvedValue([
      {
        id: "branch-1",
        key: "berlin-mitte",
        displayName: "Berlin Mitte",
        status: "ACTIVE"
      }
    ]);

    const wrapper = mount(BranchesView);
    await flushPromises();

    expect(accessApi.fetchBranches).toHaveBeenCalledWith("tenant-default", "token-123");
    expect(wrapper.text()).toContain("Berlin Mitte");
  });
});
