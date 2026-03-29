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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lombardio.platform.bootstrap.PlatformSeedFixtures;
import io.lombardio.platform.iam.application.KeycloakService;
import io.lombardio.platform.integration.application.PlatformOutboxService;
import io.lombardio.platform.tenant.api.BranchResponse;
import io.lombardio.platform.tenant.api.CreateTenantRequest;
import io.lombardio.platform.tenant.api.CreateTenantUserRequest;
import io.lombardio.platform.tenant.api.TenantUserResponse;
import io.lombardio.platform.tenant.api.UpdateTenantUserRequest;
import io.lombardio.platform.tenant.api.UpsertTenantFeatureRequest;
import io.lombardio.platform.tenant.application.support.InMemoryTenantRepositories;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TenantCatalogServiceTest {

  private final InMemoryTenantRepositories.Tenants tenants =
      new InMemoryTenantRepositories.Tenants();
  private final InMemoryTenantRepositories.Features features =
      new InMemoryTenantRepositories.Features();
  private final InMemoryTenantRepositories.Branches branches =
      new InMemoryTenantRepositories.Branches();
  private final InMemoryTenantRepositories.OutboxEvents outboxEvents =
      new InMemoryTenantRepositories.OutboxEvents();
  private final Clock clock = Clock.fixed(Instant.parse("2026-03-18T12:00:00Z"), ZoneOffset.UTC);
  private final KeycloakService keycloakService = mock(KeycloakService.class);

  private TenantCatalogService tenantCatalogService;

  @BeforeEach
  void setUp() {
    tenants.save(PlatformSeedFixtures.defaultTenant());
    PlatformSeedFixtures.defaultTenantFeatures().forEach(features::save);
    tenantCatalogService =
        new TenantCatalogService(
            tenants,
            features,
            branches,
            new PlatformOutboxService(outboxEvents, clock),
            keycloakService,
            new ObjectMapper(),
            clock);
    PlatformSeedFixtures.defaultTenantBranches().forEach(branches::save);
  }

  @Test
  void shouldCreateTenant() {
    var created =
        tenantCatalogService.createTenant(
            new CreateTenantRequest("alpha", "Pfandhaus Alpha", "ACTIVE"));

    assertEquals("alpha", created.key());
    assertEquals(2, tenantCatalogService.listTenants().size());
    assertEquals(1, outboxEvents.findAll().size());
  }

  @Test
  void shouldRejectUnsupportedFeature() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            tenantCatalogService.upsertFeature(
                "tenant-default", "unsupported-module", new UpsertTenantFeatureRequest(true)));
  }

  @Test
  void shouldListTenantUsersFromIdentityDirectory() {
    when(keycloakService.listTenantUsers("tenant-default"))
        .thenReturn(
            List.of(
                new TenantUserResponse(
                    "user-1",
                    "admin@lombardio.local",
                    "admin@lombardio.local",
                    "Tenant Admin",
                    "ACTIVE",
                    List.of("users.write", "roles.write"),
                    List.of())));

    List<TenantUserResponse> users = tenantCatalogService.listTenantUsers("tenant-default");

    assertEquals(1, users.size());
    assertEquals("Tenant Admin", users.getFirst().displayName());
  }

  @Test
  void shouldCreateTenantUserViaIdentityDirectory() {
    when(keycloakService.createTenantUser(
            "tenant-default",
            "ops@lombardio.local",
            "TempPass123!",
            "Operations",
            List.of("users.write"),
            List.of("branch-default-hq")))
        .thenReturn(
            new TenantUserResponse(
                "user-2",
                "ops@lombardio.local",
                "ops@lombardio.local",
                "Operations",
                "ACTIVE",
                List.of("users.write"),
                List.of("branch-default-hq")));

    TenantUserResponse created =
        tenantCatalogService.createTenantUser(
            "tenant-default",
            new CreateTenantUserRequest(
                "ops@lombardio.local",
                "TempPass123!",
                "Operations",
                List.of("users.write"),
                List.of("branch-default-hq")));

    assertEquals("Operations", created.displayName());
    assertEquals(List.of("users.write"), created.roleIds());
    assertEquals(List.of("branch-default-hq"), created.branchIds());
  }

  @Test
  void shouldUpdateTenantUserViaIdentityDirectory() {
    when(keycloakService.updateTenantUser(
            "tenant-default",
            "user-2",
            "ops@lombardio.local",
            "Operations Lead",
            "ACTIVE",
            List.of("users.write", "roles.write"),
            List.of("branch-default-hq", "branch-default-berlin")))
        .thenReturn(
            new TenantUserResponse(
                "user-2",
                "ops@lombardio.local",
                "ops@lombardio.local",
                "Operations Lead",
                "ACTIVE",
                List.of("users.write", "roles.write"),
                List.of("branch-default-hq", "branch-default-berlin")));

    TenantUserResponse updated =
        tenantCatalogService.updateTenantUser(
            "tenant-default",
            "user-2",
            new UpdateTenantUserRequest(
                "ops@lombardio.local",
                "Operations Lead",
                "ACTIVE",
                List.of("users.write", "roles.write"),
                List.of("branch-default-hq", "branch-default-berlin")));

    assertEquals("Operations Lead", updated.displayName());
    assertEquals(2, updated.roleIds().size());
    assertEquals(2, updated.branchIds().size());
  }

  @Test
  void shouldListBranchesForTenant() {
    List<BranchResponse> branchResponses = tenantCatalogService.listBranches("tenant-default");

    assertEquals(2, branchResponses.size());
    assertEquals("Headquarters", branchResponses.getFirst().displayName());
  }

  @Test
  void shouldLimitTenantRoleCatalogToTenantAssignableRoles() {
    when(keycloakService.getAvailableRoles())
        .thenReturn(
            List.of(
                "users.read",
                "roles.write",
                "branches.read",
                "platform.tenants.read",
                "sessions.impersonate.platform",
                "offline_access",
                "uma_authorization"));

    List<String> roles = tenantCatalogService.listAvailableRolesForTenant("tenant-default");

    assertEquals(List.of("users.read", "roles.write", "branches.read"), roles);
  }
}
