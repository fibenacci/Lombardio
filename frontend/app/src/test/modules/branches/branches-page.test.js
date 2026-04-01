import { flushPromises, mount } from "@vue/test-utils";
import BranchesView from "../../../modules/branches/ui/pages/branches-page";
import { setLocale } from "../../../app/i18n";
import { useAuthStore } from "../../../app/session/state";
import { useTenantStore } from "../../../app/tenant-context/state";
import * as accessApi from "../../../modules/access-management/infrastructure/api/access.api";

describe("BranchesView", () => {
  let authStore;
  let tenantStore;

  beforeEach(() => {
    setLocale("en");
    authStore = useAuthStore();
    tenantStore = useTenantStore();
  });

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
    expect(wrapper.text()).toContain("Create branch");
  });

  it("creates branches via the tenant API", async () => {
    authStore.token = "token-123";
    authStore.user = {
      id: "user-admin",
      tenantId: "tenant-default",
      permissions: ["branches.read", "branches.write"]
    };
    tenantStore.selectedTenantId = "tenant-default";

    vi.spyOn(accessApi, "fetchBranches").mockResolvedValue([]);
    vi.spyOn(accessApi, "createBranch").mockResolvedValue({ id: "branch-2" });

    const wrapper = mount(BranchesView);
    await flushPromises();

    const inputs = wrapper.findAll("input");
    await inputs[0].setValue("hh");
    await inputs[1].setValue("Hamburg");
    await wrapper.find("form").trigger("submit.prevent");
    await flushPromises();

    expect(accessApi.createBranch).toHaveBeenCalledWith(
      "tenant-default",
      { key: "hh", displayName: "Hamburg", status: "ACTIVE" },
      "token-123"
    );
    expect(wrapper.text()).toContain("Branch created");
  });
});
