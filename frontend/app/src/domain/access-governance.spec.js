import { describeFeature, loadFeature } from "@amiceli/vitest-cucumber";
import { expect } from "vitest";
import {
  canManagePlatformTenants,
  canManageTenantFeatures,
  canManageTenantRoles,
  canManageTenantUsers
} from "./access-policy";

const feature = await loadFeature("src/domain/access-governance.feature");

describeFeature(feature, ({ Scenario }) => {
  Scenario("Platform administrators govern tenants across the platform", ({ Given, When, Then, And }) => {
    let user;
    let tenantId;

    Given("a platform administrator without a tenant scope", () => {
      user = {
        id: "platform-admin-1",
        tenantId: null,
        permissions: ["platform.tenants.read", "platform.tenants.write"]
      };
    });

    When("platform access is evaluated for tenant {string}", (ctx, selectedTenantId) => {
      tenantId = selectedTenantId;
    });

    Then("tenant and feature management should be allowed", () => {
      expect(canManagePlatformTenants(user)).toBe(true);
      expect(canManageTenantFeatures(user)).toBe(true);
    });

    And("tenant user and role management should be allowed", () => {
      expect(canManageTenantUsers(user, tenantId)).toBe(true);
      expect(canManageTenantRoles(user, tenantId)).toBe(true);
    });
  });

  Scenario("Tenant administrators govern users and roles only inside their own tenant", ({ Given, When, Then, But }) => {
    let user;
    let tenantId;

    Given("a tenant administrator for tenant {string}", (ctx, effectiveTenantId) => {
      user = {
        id: "tenant-admin-1",
        tenantId: effectiveTenantId,
        permissions: ["users.read", "users.write", "roles.read", "roles.write"]
      };
    });

    When("tenant access is evaluated for tenant {string}", (ctx, selectedTenantId) => {
      tenantId = selectedTenantId;
    });

    Then("tenant user and role management should be allowed", () => {
      expect(canManageTenantUsers(user, tenantId)).toBe(true);
      expect(canManageTenantRoles(user, tenantId)).toBe(true);
    });

    But("tenant and feature management should not be allowed", () => {
      expect(canManagePlatformTenants(user)).toBe(false);
      expect(canManageTenantFeatures(user)).toBe(false);
    });
  });

  Scenario("Tenant administrators cannot manage another tenant", ({ Given, When, Then }) => {
    let user;
    let tenantId;

    Given("a tenant administrator for tenant {string}", (ctx, effectiveTenantId) => {
      user = {
        id: "tenant-admin-1",
        tenantId: effectiveTenantId,
        permissions: ["users.read", "users.write", "roles.read", "roles.write"]
      };
    });

    When("tenant access is evaluated for tenant {string}", (ctx, selectedTenantId) => {
      tenantId = selectedTenantId;
    });

    Then("tenant user and role management should not be allowed", () => {
      expect(canManageTenantUsers(user, tenantId)).toBe(false);
      expect(canManageTenantRoles(user, tenantId)).toBe(false);
    });
  });
});
