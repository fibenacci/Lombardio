import { flushPromises, mount } from "@vue/test-utils";
import UsersView from "../../../modules/users/ui/pages/users-page";
import { setLocale } from "../../../app/i18n";
import { useAuthStore } from "../../../app/session/state";
import { useTenantStore } from "../../../app/tenant-context/state";
import * as accessApi from "../../../modules/access-management/infrastructure/api/access.api";

describe("UsersView", () => {
  let authStore;
  let tenantStore;

  beforeEach(() => {
    setLocale("en");
    authStore = useAuthStore();
    tenantStore = useTenantStore();
  });

  it("loads the tenant user directory for tenant admins", async () => {
    authStore.token = "token-123";
    authStore.user = {
      id: "user-admin",
      tenantId: "tenant-default",
      permissions: ["users.read", "roles.read", "users.write", "sessions.impersonate.tenant"]
    };
    tenantStore.selectedTenantId = "tenant-default";

    vi.spyOn(accessApi, "fetchUsers").mockResolvedValue([
      {
        id: "user-1",
        username: "admin@lombardio.local",
        email: "admin@lombardio.local",
        displayName: "Tenant Admin",
        status: "ACTIVE",
        roleIds: ["users.write"],
        branchIds: []
      }
    ]);
    vi.spyOn(accessApi, "fetchRoles").mockResolvedValue([
      {
        id: "users.write",
        key: "users.write",
        displayName: "users.write",
        description: "",
        active: true,
        permissionKeys: []
      }
    ]);
    vi.spyOn(accessApi, "fetchBranches").mockResolvedValue([
      {
        id: "branch-1",
        key: "hq",
        displayName: "Headquarters",
        status: "ACTIVE"
      }
    ]);

    const wrapper = mount(UsersView);
    await flushPromises();

    expect(accessApi.fetchUsers).toHaveBeenCalledWith("tenant-default", "token-123");
    expect(accessApi.fetchRoles).toHaveBeenCalledWith("tenant-default", "token-123");
    expect(accessApi.fetchBranches).toHaveBeenCalledWith("tenant-default", "token-123");
    expect(wrapper.text()).toContain("Tenant Admin");
  });

  it("creates a user via the tenant provisioning API", async () => {
    authStore.token = "token-123";
    authStore.user = {
      id: "user-admin",
      tenantId: "tenant-default",
      permissions: ["users.read", "users.write", "roles.read"]
    };
    tenantStore.selectedTenantId = "tenant-default";

    vi.spyOn(accessApi, "fetchUsers").mockResolvedValue([]);
    vi.spyOn(accessApi, "fetchRoles").mockResolvedValue([
      {
        id: "role-admin",
        key: "admin",
        displayName: "Administrator",
        description: "Full access",
        active: true,
        permissionKeys: ["users.read"]
      }
    ]);
    vi.spyOn(accessApi, "fetchBranches").mockResolvedValue([
      {
        id: "branch-1",
        key: "hq",
        displayName: "Headquarters",
        status: "ACTIVE"
      },
      {
        id: "branch-2",
        key: "berlin",
        displayName: "Berlin",
        status: "ACTIVE"
      }
    ]);
    vi.spyOn(accessApi, "createUser").mockResolvedValue({
      id: "user-2"
    });

    const wrapper = mount(UsersView);
    await flushPromises();

    const inputs = wrapper.findAll("input");
    await inputs[0].setValue("ops");
    await inputs[1].setValue("ops@lombardio.local");
    await inputs[2].setValue("TempPass123!");
    await inputs[3].setValue("Operations");
    wrapper.vm.form.roleIds = ["role-admin"];
    wrapper.vm.form.branchIds = ["branch-1"];
    await wrapper.find("form").trigger("submit.prevent");
    await flushPromises();

    expect(accessApi.createUser).toHaveBeenCalledWith(
      "tenant-default",
      {
        email: "ops@lombardio.local",
        password: "TempPass123!",
        displayName: "Operations",
        roles: ["role-admin"],
        branchIds: ["branch-1"]
      },
      "token-123"
    );
    expect(wrapper.text()).toContain("User created");
  });

  it("loads role options for tenant admins", async () => {
    authStore.token = "token-123";
    authStore.user = {
      id: "user-admin",
      tenantId: "tenant-default",
      permissions: ["users.read", "users.write", "roles.read", "sessions.impersonate.tenant"]
    };
    tenantStore.selectedTenantId = "tenant-default";

    vi.spyOn(accessApi, "fetchUsers").mockResolvedValue([]);
    vi.spyOn(accessApi, "fetchRoles").mockResolvedValue([
      {
        id: "role-admin",
        key: "admin",
        displayName: "Administrator",
        description: "Full access",
        active: true,
        permissionKeys: ["users.read"]
      }
    ]);
    vi.spyOn(accessApi, "fetchBranches").mockResolvedValue([]);

    const wrapper = mount(UsersView);
    await flushPromises();

    expect(accessApi.fetchRoles).toHaveBeenCalledWith("tenant-default", "token-123");
    expect(wrapper.text()).toContain("Assign tenant roles");
  });

  it("allows editing an existing tenant user", async () => {
    authStore.token = "token-123";
    authStore.user = {
      id: "user-admin",
      tenantId: "tenant-default",
      permissions: ["users.read", "users.write", "roles.read"]
    };
    tenantStore.selectedTenantId = "tenant-default";

    vi.spyOn(accessApi, "fetchUsers").mockResolvedValue([
      {
        id: "user-7",
        username: "ops@lombardio.local",
        email: "ops@lombardio.local",
        displayName: "Operations",
        status: "ACTIVE",
        roleIds: ["users.write"],
        branchIds: ["branch-1"]
      }
    ]);
    vi.spyOn(accessApi, "fetchRoles").mockResolvedValue([
      {
        id: "users.write",
        key: "users.write",
        displayName: "users.write",
        description: "",
        active: true,
        permissionKeys: []
      },
      {
        id: "roles.write",
        key: "roles.write",
        displayName: "roles.write",
        description: "",
        active: true,
        permissionKeys: []
      }
    ]);
    vi.spyOn(accessApi, "fetchBranches").mockResolvedValue([
      {
        id: "branch-1",
        key: "hq",
        displayName: "Headquarters",
        status: "ACTIVE"
      },
      {
        id: "branch-2",
        key: "berlin",
        displayName: "Berlin",
        status: "ACTIVE"
      }
    ]);
    vi.spyOn(accessApi, "updateUser").mockResolvedValue({
      id: "user-7"
    });

    const wrapper = mount(UsersView);
    await flushPromises();

    await wrapper.vm.startEdit({
      id: "user-7",
      username: "ops@lombardio.local",
      email: "ops@lombardio.local",
      displayName: "Operations",
      status: "ACTIVE",
      roleIds: ["users.write"],
      branchIds: ["branch-1"]
    });
    wrapper.vm.form.displayName = "Operations Lead";
    wrapper.vm.form.roleIds = ["users.write", "roles.write"];
    wrapper.vm.form.branchIds = ["branch-1", "branch-2"];
    await wrapper.find("form").trigger("submit.prevent");
    await flushPromises();

    expect(accessApi.updateUser).toHaveBeenCalledWith(
      "user-7",
      {
        tenantId: "tenant-default",
        username: "ops@lombardio.local",
        password: "",
        email: "ops@lombardio.local",
        displayName: "Operations Lead",
        status: "ACTIVE",
        roleIds: ["users.write", "roles.write"],
        roles: ["users.write", "roles.write"],
        branchIds: ["branch-1", "branch-2"]
      },
      "token-123"
    );
    expect(wrapper.text()).toContain("User updated");
  });
});
