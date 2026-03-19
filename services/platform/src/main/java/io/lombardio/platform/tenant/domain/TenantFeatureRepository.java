package io.lombardio.platform.tenant.domain;

import java.util.List;
import java.util.Optional;

public interface TenantFeatureRepository {

    List<TenantFeature> findByTenantId(String tenantId);

    Optional<TenantFeature> findByTenantIdAndFeatureKey(String tenantId, String featureKey);

    TenantFeature save(TenantFeature tenantFeature);
}
