package io.lombardio.platform.infrastructure.persistence;

import io.lombardio.platform.tenant.domain.TenantFeature;
import io.lombardio.platform.tenant.domain.TenantFeatureRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TenantFeaturePersistenceAdapter implements TenantFeatureRepository {

    private final SpringDataTenantFeatureRepository repository;

    public TenantFeaturePersistenceAdapter(SpringDataTenantFeatureRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<TenantFeature> findByTenantId(String tenantId) {
        return repository.findByIdTenantIdOrderByIdFeatureKey(tenantId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<TenantFeature> findByTenantIdAndFeatureKey(String tenantId, String featureKey) {
        return repository.findById(new TenantFeatureId(tenantId, featureKey)).map(this::toDomain);
    }

    @Override
    public TenantFeature save(TenantFeature tenantFeature) {
        return toDomain(repository.save(toEntity(tenantFeature)));
    }

    private TenantFeatureEntity toEntity(TenantFeature tenantFeature) {
        TenantFeatureEntity entity = new TenantFeatureEntity();
        entity.setId(new TenantFeatureId(tenantFeature.tenantId(), tenantFeature.featureKey()));
        entity.setEnabled(tenantFeature.enabled());
        entity.setUpdatedAt(tenantFeature.updatedAt());
        return entity;
    }

    private TenantFeature toDomain(TenantFeatureEntity entity) {
        return new TenantFeature(
                entity.getId().getTenantId(),
                entity.getId().getFeatureKey(),
                entity.isEnabled(),
                entity.getUpdatedAt()
        );
    }
}
