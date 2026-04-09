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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.lombardio.platform.security.AuthenticatedUser;
import io.lombardio.platform.tenant.application.BranchView;
import io.lombardio.platform.tenant.application.TenantCatalogService;
import io.lombardio.platform.tenant.application.TenantUserView;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TenantAdministrationControllerTest {

  private final TenantCatalogService tenantCatalogService = mock(TenantCatalogService.class);

  private TenantAdministrationController controller;

  @BeforeEach
  void setUp() {
    controller = new TenantAdministrationController(tenantCatalogService);
  }

  @Test
  void tenantAdminCanListUsersOfOwnTenant() {
    AuthenticatedUser user =
        new AuthenticatedUser(
            "tenant-admin",
            "tenant-admin",
            "tenant-default",
            false,
            "admin@lombardio.local",
            "Tenant Admin",
            List.of("users.read", "users.write", "roles.read"));
    List<TenantUserResponse> expected =
        List.of(
            new TenantUserResponse(
                "user-1",
                "admin@lombardio.local",
                "admin@lombardio.local",
                "Tenant Admin",
                "ACTIVE",
                List.of("users.write"),
                List.of()));
    when(tenantCatalogService.listTenantUsers(user, "tenant-default"))
        .thenReturn(
            List.of(
                new TenantUserView(
                    "user-1",
                    "admin@lombardio.local",
                    "admin@lombardio.local",
                    "Tenant Admin",
                    "ACTIVE",
                    List.of("users.write"),
                    List.of())));

    List<TenantUserResponse> actual = controller.listUsers("tenant-default", user);

    verify(tenantCatalogService).listTenantUsers(user, "tenant-default");
    assertEquals(expected, actual);
  }

  @Test
  void tenantAdminCanListTenantRoles() {
    AuthenticatedUser user =
        new AuthenticatedUser(
            "tenant-admin",
            "tenant-admin",
            "tenant-default",
            false,
            "admin@lombardio.local",
            "Tenant Admin",
            List.of("roles.read"));
    when(tenantCatalogService.listAvailableRolesForTenant(user, "tenant-default"))
        .thenReturn(List.of("users.read", "roles.read"));

    List<String> actual = controller.listRoles("tenant-default", user);

    verify(tenantCatalogService).listAvailableRolesForTenant(user, "tenant-default");
    assertEquals(List.of("users.read", "roles.read"), actual);
  }

  @Test
  void tenantAdminCanListBranches() {
    AuthenticatedUser user =
        new AuthenticatedUser(
            "tenant-admin",
            "tenant-admin",
            "tenant-default",
            false,
            "admin@lombardio.local",
            "Tenant Admin",
            List.of("branches.read"));
    List<BranchResponse> expected =
        List.of(new BranchResponse("branch-1", "hq", "Headquarters", "ACTIVE"));
    when(tenantCatalogService.listBranches(user, "tenant-default"))
        .thenReturn(List.of(new BranchView("branch-1", "hq", "Headquarters", "ACTIVE")));

    List<BranchResponse> actual = controller.listBranches("tenant-default", user);

    verify(tenantCatalogService).listBranches(user, "tenant-default");
    assertEquals(expected, actual);
  }

  @Test
  void rejectsCrossTenantUserListingWhenAuthorizationFails() {
    AuthenticatedUser user =
        new AuthenticatedUser(
            "tenant-admin",
            "tenant-admin",
            "tenant-default",
            false,
            "admin@lombardio.local",
            "Tenant Admin",
            List.of("users.read"));

    when(tenantCatalogService.listTenantUsers(user, "tenant-hamburg"))
        .thenThrow(
            new io.lombardio.platform.security.ForbiddenException("Tenant access is limited"));

    assertThrows(
        io.lombardio.platform.security.ForbiddenException.class,
        () -> controller.listUsers("tenant-hamburg", user));
  }
}
