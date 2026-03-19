package io.lombardio.identityaccess.infrastructure.persistence;

import io.lombardio.identityaccess.access.domain.Role;
import io.lombardio.identityaccess.access.domain.RoleRepository;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Repository
public class RolePersistenceAdapter implements RoleRepository {

    private final SpringDataRoleRepository repository;
    private final SpringDataPermissionRepository permissionRepository;

    public RolePersistenceAdapter(
            SpringDataRoleRepository repository,
            SpringDataPermissionRepository permissionRepository
    ) {
        this.repository = repository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public List<Role> findAll() {
        return repository.findAllByOrderByDisplayNameAsc().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Role> findById(String id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Role> findByKey(String key) {
        return repository.findByKeyOrderByTenantIdAsc(key).stream()
                .findFirst()
                .map(this::toDomain);
    }

    @Override
    public Role save(Role role) {
        return toDomain(repository.save(toEntity(role)));
    }

    private RoleEntity toEntity(Role role) {
        RoleEntity entity = new RoleEntity();
        entity.setId(role.id());
        entity.setTenantId(role.tenantId());
        entity.setKey(role.key());
        entity.setDisplayName(role.displayName());
        entity.setDescription(role.description());
        entity.setActive(role.active());
        entity.setPermissions(new LinkedHashSet<>(StreamSupport.stream(
                permissionRepository.findAllById(role.permissionKeys()).spliterator(),
                false
        ).toList()));
        return entity;
    }

    private Role toDomain(RoleEntity entity) {
        return new Role(
                entity.getId(),
                entity.getTenantId(),
                entity.getKey(),
                entity.getDisplayName(),
                entity.getDescription(),
                entity.isActive(),
                entity.getPermissions().stream()
                        .map(PermissionEntity::getKey)
                        .sorted()
                        .toList()
        );
    }
}
