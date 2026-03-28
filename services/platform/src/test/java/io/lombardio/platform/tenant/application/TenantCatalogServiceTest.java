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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lombardio.platform.bootstrap.PlatformSeedFixtures;
import io.lombardio.platform.iam.application.KeycloakService;
import io.lombardio.platform.integration.application.PlatformOutboxService;
import io.lombardio.platform.tenant.api.CreateTenantRequest;
import io.lombardio.platform.tenant.api.UpsertTenantFeatureRequest;
import io.lombardio.platform.tenant.application.support.InMemoryTenantRepositories;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TenantCatalogServiceTest {

  private final InMemoryTenantRepositories.Tenants tenants =
      new InMemoryTenantRepositories.Tenants();
  private final InMemoryTenantRepositories.Features features =
      new InMemoryTenantRepositories.Features();
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
            new PlatformOutboxService(outboxEvents, clock),
            keycloakService,
            new ObjectMapper(),
            clock);
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
}
