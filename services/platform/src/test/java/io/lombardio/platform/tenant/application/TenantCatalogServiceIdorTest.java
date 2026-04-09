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
package io.lombardio.platform.tenant.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import io.lombardio.platform.security.AuthenticatedUser;
import io.lombardio.platform.security.ForbiddenException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TenantCatalogServiceIdorTest {

  private TenantCatalogService tenantCatalogService;
  private final TenantLifecycleService tenantLifecycleService = mock(TenantLifecycleService.class);
  private final TenantFeatureService tenantFeatureService = mock(TenantFeatureService.class);
  private final TenantBranchService tenantBranchService = mock(TenantBranchService.class);
  private final TenantUserService tenantUserService = mock(TenantUserService.class);
  private final TenantAdministrationAuthorizationService authorizationService =
      new TenantAdministrationAuthorizationService();

  @BeforeEach
  void setUp() {
    tenantCatalogService =
        new TenantCatalogService(
            tenantLifecycleService,
            tenantFeatureService,
            tenantBranchService,
            tenantUserService,
            authorizationService);
  }

  @Test
  void shouldRejectAccessToOtherTenant() {
    // GIVEN: User belongs to tenant-1
    AuthenticatedUser user =
        new AuthenticatedUser(
            "user-1",
            "user-1",
            "tenant-1",
            false,
            "user@tenant1.com",
            "User 1",
            List.of("users.read"));

    // WHEN: Attempting to list users for tenant-2
    // THEN: It should throw ForbiddenException
    assertThrows(
        ForbiddenException.class, () -> tenantCatalogService.listTenantUsers(user, "tenant-2"));
  }

  @Test
  void shouldAllowAccessToOwnTenant() {
    // GIVEN: User belongs to tenant-1
    AuthenticatedUser user =
        new AuthenticatedUser(
            "user-1",
            "user-1",
            "tenant-1",
            false,
            "user@tenant1.com",
            "User 1",
            List.of("users.read"));

    // WHEN: Attempting to list users for tenant-1
    // THEN: It should not throw (it calls the underlying service which is mocked and returns null
    // by default)
    tenantCatalogService.listTenantUsers(user, "tenant-1");
  }
}
