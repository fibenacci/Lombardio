package io.lombardio.identityaccess.infrastructure.persistence;

import io.lombardio.identityaccess.access.domain.Permission;
import io.lombardio.identityaccess.access.domain.PermissionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PermissionPersistenceAdapter implements PermissionRepository {

    private final SpringDataPermissionRepository repository;

    public PermissionPersistenceAdapter(SpringDataPermissionRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Permission> findAll() {
        return repository.findAllByOrderByKeyAsc().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Permission> findByKey(String key) {
        return repository.findById(key).map(this::toDomain);
    }

    @Override
    public Permission save(Permission permission) {
        return toDomain(repository.save(toEntity(permission)));
    }

    private PermissionEntity toEntity(Permission permission) {
        PermissionEntity entity = new PermissionEntity();
        entity.setKey(permission.key());
        entity.setDisplayName(permission.displayName());
        entity.setDescription(permission.description());
        return entity;
    }

    private Permission toDomain(PermissionEntity entity) {
        return new Permission(
                entity.getKey(),
                entity.getDisplayName(),
                entity.getDescription()
        );
    }
}
