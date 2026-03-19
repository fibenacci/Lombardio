package io.lombardio.platform.demo;

import io.lombardio.platform.tenant.domain.TenantFeatureRepository;
import io.lombardio.platform.tenant.domain.TenantRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
class ScenarioDataSeeder {

    private final TenantRepository tenantRepository;
    private final TenantFeatureRepository tenantFeatureRepository;
    private final DemoDataProperties demoDataProperties;

    ScenarioDataSeeder(
            TenantRepository tenantRepository,
            TenantFeatureRepository tenantFeatureRepository,
            DemoDataProperties demoDataProperties
    ) {
        this.tenantRepository = tenantRepository;
        this.tenantFeatureRepository = tenantFeatureRepository;
        this.demoDataProperties = demoDataProperties;
    }

    void seed() {
        Instant timestamp = Instant.now().minusSeconds(86_400);
        for (var tenantDefinition : PlatformSeedFixtures.tenantsForScale(demoDataProperties.effectiveScale())) {
            var tenant = tenantRepository.save(PlatformSeedFixtures.toTenant(tenantDefinition, timestamp));
            for (var feature : PlatformSeedFixtures.tenantFeatures(tenantDefinition, timestamp)) {
                tenantFeatureRepository.save(feature);
            }
        }
    }
}
