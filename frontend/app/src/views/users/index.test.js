import { flushPromises, mount } from "@vue/test-utils";
import UsersView from ".";
import { authStore } from "../../stores/auth";
import { tenantStore } from "../../stores/tenant";
import * as accessApi from "../../services/api/access";

describe("UsersView", () => {
  it("loads users and roles from the live API layer", async () => {
    authStore.token = "token-123";
    authStore.user = {
      id: "user-admin",
      tenantId: "tenant-default",
      permissions: ["users.read", "roles.read", "users.write", "sessions.impersonate.tenant"]
    };
    tenantStore.selectedTenantId = "tenant-default";

    vi.spyOn(accessApi, "fetchUsers").mockResolvedValue([
      {
        id: "user-admin",
        username: "admin",
        email: "admin@lombardio.local",
        displayName: "System Admin",
        status: "ACTIVE",
        roleIds: ["role-admin"]
      }
    ]);
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
      }
    ]);

    const wrapper = mount(UsersView);
    await flushPromises();

    expect(accessApi.fetchUsers).toHaveBeenCalledWith("tenant-default", "token-123");
    expect(accessApi.fetchRoles).toHaveBeenCalledWith("tenant-default", "token-123");
    expect(wrapper.text()).toContain("System Admin");
    expect(wrapper.text()).toContain("Administrator");
  });

  it("creates a user via the backend API and reloads the table", async () => {
    authStore.token = "token-123";
    authStore.user = {
      id: "user-admin",
      tenantId: "tenant-default",
      permissions: ["users.read", "roles.read", "users.write", "sessions.impersonate.tenant"]
    };
    tenantStore.selectedTenantId = "tenant-default";

    vi.spyOn(accessApi, "fetchUsers")
      .mockResolvedValueOnce([])
      .mockResolvedValueOnce([
        {
          id: "user-2",
          username: "ops",
          email: "ops@lombardio.local",
          displayName: "Operations",
          status: "ACTIVE",
          roleIds: ["role-admin"]
        }
      ]);
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
    const checkboxes = wrapper.findAll('input[type="checkbox"]');
    await checkboxes[0].setValue(true);
    await checkboxes[1].setValue(true);
    await wrapper.find("form").trigger("submit.prevent");
    await flushPromises();

    expect(accessApi.createUser).toHaveBeenCalledWith(
      "tenant-default",
      {
        branchIds: ["branch-1"],
        username: "ops",
        email: "ops@lombardio.local",
        initialPassword: "TempPass123!",
        displayName: "Operations",
        status: "ACTIVE",
        roleIds: ["role-admin"]
      },
      "token-123"
    );
    expect(wrapper.text()).toContain("User created");
    expect(wrapper.text()).toContain("Operations");
  });

  it("starts a delegated session from the user directory", async () => {
    authStore.token = "token-123";
    authStore.user = {
      id: "user-platform-admin",
      tenantId: "tenant-platform",
      permissions: ["users.read", "roles.read", "platform.tenants.read", "sessions.impersonate.platform"]
    };
    tenantStore.selectedTenantId = "tenant-default";
    tenantStore.tenants = [{ id: "tenant-default", displayName: "Default Tenant" }];

    vi.spyOn(accessApi, "fetchUsers").mockResolvedValue([
      {
        id: "user-admin",
        username: "admin",
        email: "admin@lombardio.local",
        displayName: "System Admin",
        status: "ACTIVE",
        roleIds: ["role-admin"]
      }
    ]);
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
    vi.spyOn(authStore, "startDelegation").mockResolvedValue({
      id: "user-admin",
      tenantId: "tenant-default"
    });
    vi.spyOn(tenantStore, "refreshTenants").mockResolvedValue();

    const wrapper = mount(UsersView);
    await flushPromises();

    await wrapper.vm.delegateToUser("user-admin");
    await flushPromises();

    expect(authStore.startDelegation).toHaveBeenCalledWith("user-admin");
  });

  it("shows the user directory and create form for platform admins", async () => {
    authStore.token = "token-123";
    authStore.user = {
      id: "user-platform-admin",
      tenantId: "tenant-platform",
      permissions: [
        "users.read",
        "users.write",
        "roles.read",
        "platform.tenants.read",
        "platform.tenants.write",
        "sessions.impersonate.platform"
      ]
    };
    tenantStore.selectedTenantId = "tenant-default";

    vi.spyOn(accessApi, "fetchUsers").mockResolvedValue([
      {
        id: "user-admin",
        username: "admin",
        email: "admin@lombardio.local",
        displayName: "System Admin",
        status: "ACTIVE",
        roleIds: ["role-admin"]
      }
    ]);
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

    expect(wrapper.text()).toContain("System Admin");
    expect(wrapper.text()).toContain("Create User");
  });

  it("updates an existing user via the backend API", async () => {
    authStore.token = "token-123";
    authStore.user = {
      id: "user-admin",
      tenantId: "tenant-default",
      permissions: ["users.read", "roles.read", "users.write", "branches.read", "branches.write"]
    };
    tenantStore.selectedTenantId = "tenant-default";

    vi.spyOn(accessApi, "fetchUsers")
      .mockResolvedValueOnce([
        {
          id: "user-2",
          username: "ops",
          email: "ops@lombardio.local",
          displayName: "Operations",
          status: "ACTIVE",
          roleIds: ["role-admin"],
          branchIds: ["branch-1"]
        }
      ])
      .mockResolvedValueOnce([
        {
          id: "user-2",
          username: "ops",
          email: "ops@lombardio.local",
          displayName: "Operations Lead",
          status: "ACTIVE",
          roleIds: ["role-admin"],
          branchIds: ["branch-1"]
        }
      ]);
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
      }
    ]);
    vi.spyOn(accessApi, "updateUser").mockResolvedValue({
      id: "user-2"
    });

    const wrapper = mount(UsersView);
    await flushPromises();

    await wrapper.vm.startEdit({
      id: "user-2",
      username: "ops",
      email: "ops@lombardio.local",
      displayName: "Operations",
      status: "ACTIVE",
      roleIds: ["role-admin"],
      branchIds: ["branch-1"]
    });
    wrapper.vm.form.displayName = "Operations Lead";
    await wrapper.find("form").trigger("submit.prevent");
    await flushPromises();

    expect(accessApi.updateUser).toHaveBeenCalledWith(
      "user-2",
      {
        branchIds: ["branch-1"],
        username: "ops",
        email: "ops@lombardio.local",
        displayName: "Operations Lead",
        status: "ACTIVE",
        roleIds: ["role-admin"]
      },
      "token-123"
    );
    expect(wrapper.text()).toContain("Operations Lead");
  });
});
