import { describe, expect, it } from "vitest";
import {
  canManagePlatformTenants,
  canManageTenantFeatures,
  canManageTenantRoles,
  canManageTenantUsers
} from "./access-policy";

describe("tenant access policy", () => {
  it("allows platform administrators to govern tenants across the platform", () => {
    const user = {
      tenantId: null,
      permissions: ["platform.tenants.read", "platform.tenants.write"]
    };

    expect(canManagePlatformTenants(user)).toBe(true);
    expect(canManageTenantFeatures(user)).toBe(true);
    expect(canManageTenantUsers(user, "tenant-default")).toBe(true);
    expect(canManageTenantRoles(user, "tenant-default")).toBe(true);
  });

  it("allows tenant administrators to manage users and roles only inside their own tenant", () => {
    const user = {
      tenantId: "tenant-default",
      permissions: ["users.read", "users.write", "roles.read", "roles.write"]
    };

    expect(canManageTenantUsers(user, "tenant-default")).toBe(true);
    expect(canManageTenantRoles(user, "tenant-default")).toBe(true);
    expect(canManagePlatformTenants(user)).toBe(false);
    expect(canManageTenantFeatures(user)).toBe(false);
  });

  it("blocks tenant administrators from managing another tenant", () => {
    const user = {
      tenantId: "tenant-default",
      permissions: ["users.read", "users.write", "roles.read", "roles.write"]
    };

    expect(canManageTenantUsers(user, "tenant-hamburg")).toBe(false);
    expect(canManageTenantRoles(user, "tenant-hamburg")).toBe(false);
  });
});
