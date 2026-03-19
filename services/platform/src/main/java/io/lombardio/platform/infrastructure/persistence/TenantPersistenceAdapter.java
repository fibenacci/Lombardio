package io.lombardio.platform.infrastructure.persistence;

import io.lombardio.platform.tenant.domain.Tenant;
import io.lombardio.platform.tenant.domain.TenantRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TenantPersistenceAdapter implements TenantRepository {

    private final SpringDataTenantRepository repository;

    public TenantPersistenceAdapter(SpringDataTenantRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Tenant> findAll() {
        return repository.findAll().stream()
                .sorted(java.util.Comparator.comparing(TenantEntity::getCreatedAt))
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Tenant> findById(String id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Tenant> findByKey(String key) {
        return repository.findByKey(key).map(this::toDomain);
    }

    @Override
    public Tenant save(Tenant tenant) {
        return toDomain(repository.save(toEntity(tenant)));
    }

    private TenantEntity toEntity(Tenant tenant) {
        TenantEntity entity = new TenantEntity();
        entity.setId(tenant.id());
        entity.setKey(tenant.key());
        entity.setDisplayName(tenant.displayName());
        entity.setStatus(tenant.status());
        entity.setCreatedAt(tenant.createdAt());
        entity.setUpdatedAt(tenant.updatedAt());
        return entity;
    }

    private Tenant toDomain(TenantEntity entity) {
        return new Tenant(
                entity.getId(),
                entity.getKey(),
                entity.getDisplayName(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
