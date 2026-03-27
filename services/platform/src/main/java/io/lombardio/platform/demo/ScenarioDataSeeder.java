package io.lombardio.platform.demo;

import io.lombardio.platform.tenant.domain.TenantFeatureRepository;
import io.lombardio.platform.tenant.domain.TenantRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class ScenarioDataSeeder {

    private static final List<PlatformSeedFixtures.DemoTenant> DEMO_TENANTS = List.of(
            new PlatformSeedFixtures.DemoTenant("tenant-default", "default", "Default Tenant", "ACTIVE", true, true, true, true, true, true, true),
            new PlatformSeedFixtures.DemoTenant("tenant-hamburg", "hanseatic", "Hanseatic Pawn Hamburg", "ACTIVE", true, true, true, true, true, true, true),
            new PlatformSeedFixtures.DemoTenant("tenant-munich", "isar", "Isar Pfand Muenchen", "ACTIVE", true, true, true, true, true, true, true),
            new PlatformSeedFixtures.DemoTenant("tenant-cologne", "rhein", "Rhein Pfand Koeln", "ACTIVE", true, true, true, false, true, false, false),
            new PlatformSeedFixtures.DemoTenant("tenant-stuttgart", "neckar", "Neckar Pfand Stuttgart", "INACTIVE", true, false, true, false, false, false, false)
    );

    private final TenantRepository tenantRepository;
    private final TenantFeatureRepository tenantFeatureRepository;

    public ScenarioDataSeeder(TenantRepository tenantRepository, TenantFeatureRepository tenantFeatureRepository) {
        this.tenantRepository = tenantRepository;
        this.tenantFeatureRepository = tenantFeatureRepository;
    }

    public void seed() {
        Instant timestamp = Instant.now().minusSeconds(86_400); // 24 hours ago

        for (var tenantDefinition : DEMO_TENANTS) {
            tenantRepository.save(PlatformSeedFixtures.toTenant(tenantDefinition, timestamp));
            for (var feature : PlatformSeedFixtures.tenantFeatures(tenantDefinition, timestamp)) {
                tenantFeatureRepository.save(feature);
            }
        }
    }

    private static PlatformSeedFixtures.DemoTenant DEFAULT_TENANT() {
        return DEMO_TENANTS.get(0);
    }
}
