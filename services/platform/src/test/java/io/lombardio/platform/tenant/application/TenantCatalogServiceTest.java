package io.lombardio.platform.tenant.application;

import io.lombardio.platform.bootstrap.PlatformSeedFixtures;
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
    private final Clock clock = Clock.fixed(Instant.parse("2026-03-18T12:00:00Z"), ZoneOffset.UTC);

    private TenantCatalogService tenantCatalogService;

    @BeforeEach
    void setUp() {
        tenants.save(PlatformSeedFixtures.defaultTenant());
        PlatformSeedFixtures.defaultTenantFeatures().forEach(features::save);
        tenantCatalogService = new TenantCatalogService(tenants, features, clock);
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
