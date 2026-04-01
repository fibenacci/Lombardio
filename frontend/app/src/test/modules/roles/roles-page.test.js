import { flushPromises, mount } from "@vue/test-utils";
import RolesView from "../../../modules/roles/ui/pages/roles-page";
import { setLocale } from "../../../app/i18n";
import { useAuthStore } from "../../../app/session/state";
import { useTenantStore } from "../../../app/tenant-context/state";
import * as accessApi from "../../../modules/access-management/infrastructure/api/access.api";

describe("RolesView", () => {
  let authStore;
  let tenantStore;

  beforeEach(() => {
    setLocale("en");
    authStore = useAuthStore();
    tenantStore = useTenantStore();
  });

  it("loads tenant roles from the API layer", async () => {
    authStore.token = "token-123";
    authStore.user = {
      id: "user-admin",
      tenantId: "tenant-default",
      permissions: ["roles.read"]
    };
    tenantStore.selectedTenantId = "tenant-default";

    vi.spyOn(accessApi, "fetchRoles").mockResolvedValue([
      {
        id: "role-admin",
        key: "admin",
        displayName: "Administrator",
        description: "Full access",
        active: true,
        permissionKeys: ["users.read", "roles.read"]
      }
    ]);

    const wrapper = mount(RolesView);
    await flushPromises();

    expect(accessApi.fetchRoles).toHaveBeenCalledWith("tenant-default", "token-123");
    expect(wrapper.text()).toContain("Administrator");
    expect(wrapper.text()).toContain("2");
  });

  it("shows the role management limitation alongside the live role list", async () => {
    authStore.token = "token-123";
    authStore.user = {
      id: "user-admin",
      tenantId: "tenant-default",
      permissions: ["roles.read", "roles.write"]
    };
    tenantStore.selectedTenantId = "tenant-default";

    vi.spyOn(accessApi, "fetchRoles").mockResolvedValue([
      {
        id: "role-2",
        key: "branch-manager",
        displayName: "Branch Manager",
        description: "Branch administration role",
        active: true,
        permissionKeys: []
      }
    ]);

    const wrapper = mount(RolesView);
    await flushPromises();

    expect(wrapper.text()).toContain("Branch Manager");
    expect(wrapper.text()).toContain("not exposed by the current platform service");
  });

  it("shows an empty-state info message when no tenant roles are available", async () => {
    authStore.token = "token-123";
    authStore.user = {
      id: "user-admin",
      tenantId: "tenant-default",
      permissions: ["roles.read"]
    };
    tenantStore.selectedTenantId = "tenant-default";

    vi.spyOn(accessApi, "fetchRoles").mockResolvedValue([]);

    const wrapper = mount(RolesView);
    await flushPromises();

    expect(wrapper.text()).toContain("No tenant roles are currently available.");
    expect(wrapper.text()).toContain("Role management");
  });
});
