package io.lombardio.platform.tenant.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lombardio.platform.demo.PlatformSeedFixtures;
import io.lombardio.platform.integration.application.PlatformOutboxService;
import io.lombardio.platform.tenant.api.CreateTenantRequest;
import io.lombardio.platform.tenant.api.UpsertTenantFeatureRequest;
import io.lombardio.platform.tenant.application.support.InMemoryTenantRepositories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TenantCatalogServiceTest {

    private final InMemoryTenantRepositories.Tenants tenants = new InMemoryTenantRepositories.Tenants();
    private final InMemoryTenantRepositories.Features features = new InMemoryTenantRepositories.Features();
    private final InMemoryTenantRepositories.OutboxEvents outboxEvents = new InMemoryTenantRepositories.OutboxEvents();
    private final Clock clock = Clock.fixed(Instant.parse("2026-03-18T12:00:00Z"), ZoneOffset.UTC);

    private TenantCatalogService tenantCatalogService;

    @BeforeEach
    void setUp() {
        tenants.save(PlatformSeedFixtures.defaultTenant());
        PlatformSeedFixtures.defaultTenantFeatures().forEach(features::save);
        tenantCatalogService = new TenantCatalogService(
                tenants,
                features,
                new PlatformOutboxService(outboxEvents, clock),
                new ObjectMapper(),
                clock
        );
    }

    @Test
    void shouldCreateTenant() {
        var created = tenantCatalogService.createTenant(new CreateTenantRequest(
                "alpha",
                "Pfandhaus Alpha",
                "ACTIVE"
        ));

        assertEquals("alpha", created.key());
        assertEquals(2, tenantCatalogService.listTenants().size());
        assertEquals(1, outboxEvents.findAll().size());
    }

    @Test
    void shouldRejectUnsupportedFeature() {
        assertThrows(IllegalArgumentException.class, () ->
                tenantCatalogService.upsertFeature(
                        "tenant-default",
                        "unsupported-module",
                        new UpsertTenantFeatureRequest(true)
                )
        );
    }
}
