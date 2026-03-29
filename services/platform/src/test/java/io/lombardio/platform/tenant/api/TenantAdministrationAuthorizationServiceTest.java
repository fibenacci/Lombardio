/*
 * Lombardio Source-Available No-Distribution License 1.0
 *
 * Copyright (c) 2026 Benjamin Letzel. All rights reserved.
 *
 * This project is source-available for educational and review purposes only.
 * Redistribution, sublicensing, or commercial use is strictly prohibited.
 *
 * For partnership or cooperation inquiries, please contact the author.
 */
package io.lombardio.platform.tenant.api;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.lombardio.platform.security.AuthenticatedUser;
import io.lombardio.platform.security.ForbiddenException;
import java.util.List;
import org.junit.jupiter.api.Test;

class TenantAdministrationAuthorizationServiceTest {

  private final TenantAdministrationAuthorizationService service =
      new TenantAdministrationAuthorizationService();

  @Test
  void allowsPlatformAdminToReadTenantRolesAcrossTenants() {
    AuthenticatedUser user =
        new AuthenticatedUser(
            "platform-admin",
            "platform-admin",
            null,
            false,
            "platform@lombardio.local",
            "Platform Admin",
            List.of("platform.tenants.read", "platform.tenants.write"));

    assertDoesNotThrow(() -> service.requireTenantRoleRead(user, "tenant-default"));
  }

  @Test
  void allowsTenantAdminToReadOwnTenantUsers() {
    AuthenticatedUser user =
        new AuthenticatedUser(
            "tenant-admin",
            "tenant-admin",
            "tenant-default",
            false,
            "admin@lombardio.local",
            "Tenant Admin",
            List.of("users.read", "users.write", "roles.read", "roles.write"));

    assertDoesNotThrow(() -> service.requireTenantUserRead(user, "tenant-default"));
  }

  @Test
  void rejectsTenantAdminAccessToAnotherTenant() {
    AuthenticatedUser user =
        new AuthenticatedUser(
            "tenant-admin",
            "tenant-admin",
            "tenant-default",
            false,
            "admin@lombardio.local",
            "Tenant Admin",
            List.of("users.read", "roles.read"));

    assertThrows(ForbiddenException.class, () -> service.requireTenantRoleRead(user, "tenant-hamburg"));
  }

  @Test
  void allowsTenantAdminToManageOwnTenantBranches() {
    AuthenticatedUser user =
        new AuthenticatedUser(
            "tenant-admin",
            "tenant-admin",
            "tenant-default",
            false,
            "admin@lombardio.local",
            "Tenant Admin",
            List.of("branches.read", "branches.write"));

    assertDoesNotThrow(() -> service.requireTenantBranchWrite(user, "tenant-default"));
  }
}
