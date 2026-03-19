package io.lombardio.identityaccess.infrastructure.persistence;

import io.lombardio.identityaccess.access.domain.User;
import io.lombardio.identityaccess.access.domain.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Repository
public class UserPersistenceAdapter implements UserRepository {

    private final SpringDataUserRepository repository;
    private final SpringDataRoleRepository roleRepository;
    private final SpringDataBranchRepository branchRepository;

    public UserPersistenceAdapter(
            SpringDataUserRepository repository,
            SpringDataRoleRepository roleRepository,
            SpringDataBranchRepository branchRepository
    ) {
        this.repository = repository;
        this.roleRepository = roleRepository;
        this.branchRepository = branchRepository;
    }

    @Override
    public List<User> findAll() {
        return repository.findAllByOrderByCreatedAtAsc().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<User> findById(String id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmailIgnoreCaseOrderByTenantIdAsc(email).stream()
                .findFirst()
                .map(this::toDomain);
    }

    @Override
    public Optional<User> findByTenantIdAndEmail(String tenantId, String email) {
        return repository.findFirstByTenantIdAndEmailIgnoreCase(tenantId, email).map(this::toDomain);
    }

    @Override
    public User save(User user) {
        return toDomain(repository.save(toEntity(user)));
    }

    private UserEntity toEntity(User user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.id());
        entity.setTenantId(user.tenantId());
        entity.setUsername(user.username());
        entity.setEmail(user.email());
        entity.setPasswordHash(user.passwordHash());
        entity.setDisplayName(user.displayName());
        entity.setStatus(user.status());
        entity.setCreatedAt(user.createdAt());
        entity.setUpdatedAt(user.updatedAt());
        entity.setRoles(new LinkedHashSet<>(StreamSupport.stream(
                roleRepository.findAllById(user.roleIds()).spliterator(),
                false
        ).toList()));
        entity.setBranches(new LinkedHashSet<>(StreamSupport.stream(
                branchRepository.findAllById(user.branchIds()).spliterator(),
                false
        ).toList()));
        return entity;
    }

    private User toDomain(UserEntity entity) {
        return new User(
                entity.getId(),
                entity.getTenantId(),
                entity.getBranches().stream()
                        .map(BranchEntity::getId)
                        .sorted()
                        .toList(),
                entity.getUsername(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getDisplayName(),
                entity.getStatus(),
                entity.getRoles().stream()
                        .map(RoleEntity::getId)
                        .sorted()
                        .toList(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
