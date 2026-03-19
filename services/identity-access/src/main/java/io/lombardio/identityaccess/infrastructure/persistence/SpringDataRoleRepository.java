package io.lombardio.identityaccess.infrastructure.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataRoleRepository extends JpaRepository<RoleEntity, String> {

    @EntityGraph(attributePaths = "permissions")
    List<RoleEntity> findAllByOrderByDisplayNameAsc();

    @Override
    @EntityGraph(attributePaths = "permissions")
    Optional<RoleEntity> findById(String id);

    @EntityGraph(attributePaths = "permissions")
    List<RoleEntity> findByKeyOrderByTenantIdAsc(String key);
}
