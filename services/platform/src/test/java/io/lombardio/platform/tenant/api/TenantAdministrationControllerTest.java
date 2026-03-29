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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.lombardio.platform.security.AuthenticatedUser;
import io.lombardio.platform.tenant.application.TenantCatalogService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TenantAdministrationControllerTest {

  private final TenantCatalogService tenantCatalogService = mock(TenantCatalogService.class);
  private final TenantAdministrationAuthorizationService authorizationService =
      mock(TenantAdministrationAuthorizationService.class);

  private TenantAdministrationController controller;

  @BeforeEach
  void setUp() {
    controller = new TenantAdministrationController(tenantCatalogService, authorizationService);
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
    when(tenantCatalogService.listTenantUsers("tenant-default")).thenReturn(expected);

    List<TenantUserResponse> actual = controller.listUsers("tenant-default", user);

    verify(authorizationService).requireTenantUserRead(user, "tenant-default");
    verify(tenantCatalogService).listTenantUsers("tenant-default");
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
    when(tenantCatalogService.listAvailableRolesForTenant("tenant-default"))
        .thenReturn(List.of("users.read", "roles.read"));

    List<String> actual = controller.listRoles("tenant-default", user);

    verify(authorizationService).requireTenantRoleRead(user, "tenant-default");
    verify(tenantCatalogService).listAvailableRolesForTenant("tenant-default");
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
    when(tenantCatalogService.listBranches("tenant-default")).thenReturn(expected);

    List<BranchResponse> actual = controller.listBranches("tenant-default", user);

    verify(authorizationService).requireTenantBranchRead(user, "tenant-default");
    verify(tenantCatalogService).listBranches("tenant-default");
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

    doThrow(new io.lombardio.platform.security.ForbiddenException("Tenant access is limited"))
        .when(authorizationService)
        .requireTenantUserRead(user, "tenant-hamburg");

    assertThrows(
        io.lombardio.platform.security.ForbiddenException.class,
        () -> controller.listUsers("tenant-hamburg", user));
    verify(tenantCatalogService, never()).listTenantUsers("tenant-hamburg");
  }
}
