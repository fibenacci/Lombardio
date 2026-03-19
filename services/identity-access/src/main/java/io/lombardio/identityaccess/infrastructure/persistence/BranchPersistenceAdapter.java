package io.lombardio.identityaccess.infrastructure.persistence;

import io.lombardio.identityaccess.access.domain.Branch;
import io.lombardio.identityaccess.access.domain.BranchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BranchPersistenceAdapter implements BranchRepository {

    private final SpringDataBranchRepository repository;

    public BranchPersistenceAdapter(SpringDataBranchRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Branch> findAll() {
        return repository.findAllByOrderByDisplayNameAsc().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Branch> findById(String id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Branch> findByTenantIdAndKey(String tenantId, String key) {
        return repository.findFirstByTenantIdAndKey(tenantId, key).map(this::toDomain);
    }

    @Override
    public Branch save(Branch branch) {
        return toDomain(repository.save(toEntity(branch)));
    }

    private BranchEntity toEntity(Branch branch) {
        BranchEntity entity = new BranchEntity();
        entity.setId(branch.id());
        entity.setTenantId(branch.tenantId());
        entity.setKey(branch.key());
        entity.setDisplayName(branch.displayName());
        entity.setStatus(branch.status());
        entity.setCreatedAt(branch.createdAt());
        entity.setUpdatedAt(branch.updatedAt());
        return entity;
    }

    private Branch toDomain(BranchEntity entity) {
        return new Branch(
                entity.getId(),
                entity.getTenantId(),
                entity.getKey(),
                entity.getDisplayName(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
