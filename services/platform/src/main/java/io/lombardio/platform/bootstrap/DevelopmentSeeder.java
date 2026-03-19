package io.lombardio.platform.bootstrap;

import io.lombardio.platform.tenant.domain.TenantFeatureRepository;
import io.lombardio.platform.tenant.domain.TenantRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DevelopmentSeeder {

    @Bean
    ApplicationRunner seedPlatformData(TenantRepository tenantRepository, TenantFeatureRepository tenantFeatureRepository) {
        return args -> {
            var tenant = tenantRepository.findById("tenant-default")
                    .orElseGet(() -> tenantRepository.save(PlatformSeedFixtures.defaultTenant()));

            PlatformSeedFixtures.defaultTenantFeatures().forEach(feature ->
                    tenantFeatureRepository.findByTenantIdAndFeatureKey(tenant.id(), feature.featureKey())
                            .orElseGet(() -> tenantFeatureRepository.save(feature))
            );
        };
    }
}
