import { flushPromises, mount } from "@vue/test-utils";
import RolesView from ".";
import { authStore } from "../../stores/auth";
import { tenantStore } from "../../stores/tenant";
import * as accessApi from "../../services/api/access";

describe("RolesView", () => {
  it("loads roles and permissions from the API layer", async () => {
    authStore.token = "token-123";
    authStore.user = {
      id: "user-admin",
      tenantId: "tenant-default",
      permissions: ["roles.read", "permissions.read", "roles.write"]
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
    vi.spyOn(accessApi, "fetchPermissions").mockResolvedValue([
      {
        key: "users.read",
        displayName: "Read users",
        description: "Allows reading user records"
      },
      {
        key: "roles.read",
        displayName: "Read roles",
        description: "Allows reading role definitions"
      }
    ]);

    const wrapper = mount(RolesView);
    await flushPromises();

    expect(accessApi.fetchRoles).toHaveBeenCalledWith("tenant-default", "token-123");
    expect(accessApi.fetchPermissions).toHaveBeenCalledWith("token-123");
    expect(wrapper.text()).toContain("Administrator");
    expect(wrapper.text()).toContain("2");
  });

  it("creates a role via the backend API and reloads the table", async () => {
    authStore.token = "token-123";
    authStore.user = {
      id: "user-admin",
      tenantId: "tenant-default",
      permissions: ["roles.read", "permissions.read", "roles.write"]
    };
    tenantStore.selectedTenantId = "tenant-default";

    vi.spyOn(accessApi, "fetchRoles")
      .mockResolvedValueOnce([])
      .mockResolvedValueOnce([
        {
          id: "role-2",
          key: "branch-manager",
          displayName: "Branch Manager",
          description: "Branch administration role",
          active: true,
          permissionKeys: ["users.read", "roles.read"]
        }
      ]);
    vi.spyOn(accessApi, "fetchPermissions").mockResolvedValue([
      {
        key: "users.read",
        displayName: "Read users",
        description: "Allows reading user records"
      },
      {
        key: "roles.read",
        displayName: "Read roles",
        description: "Allows reading role definitions"
      }
    ]);
    vi.spyOn(accessApi, "createRole").mockResolvedValue({
      id: "role-2"
    });

    const wrapper = mount(RolesView);
    await flushPromises();

    const textInputs = wrapper.findAll('input[type="text"]');
    await textInputs[0].setValue("branch-manager");
    await textInputs[1].setValue("Branch Manager");
    await wrapper.find("textarea").setValue("Branch administration role");
    wrapper.vm.form.permissionKeys = ["users.read", "roles.read"];
    await wrapper.find("form").trigger("submit.prevent");
    await flushPromises();

    expect(accessApi.createRole).toHaveBeenCalledWith(
      "tenant-default",
      {
        key: "branch-manager",
        displayName: "Branch Manager",
        description: "Branch administration role",
        active: true,
        permissionKeys: ["users.read", "roles.read"]
      },
      "token-123"
    );
    expect(wrapper.text()).toContain("Role created");
    expect(wrapper.text()).toContain("Branch Manager");
  });

  it("shows the role directory and create form for platform admins", async () => {
    authStore.token = "token-123";
    authStore.user = {
      id: "user-platform-admin",
      tenantId: "tenant-platform",
      permissions: ["roles.read", "roles.write", "permissions.read", "platform.tenants.read", "platform.tenants.write"]
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
    vi.spyOn(accessApi, "fetchPermissions").mockResolvedValue([
      {
        key: "users.read",
        displayName: "Read users",
        description: "Allows reading user records"
      }
    ]);

    const wrapper = mount(RolesView);
    await flushPromises();

    expect(wrapper.text()).toContain("Administrator");
    expect(wrapper.text()).toContain("Create Role");
  });
});
