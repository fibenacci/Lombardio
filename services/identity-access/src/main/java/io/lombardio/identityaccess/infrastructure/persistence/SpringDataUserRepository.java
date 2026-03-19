package io.lombardio.identityaccess.infrastructure.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataUserRepository extends JpaRepository<UserEntity, String> {

    @EntityGraph(attributePaths = {"roles", "branches"})
    List<UserEntity> findAllByOrderByCreatedAtAsc();

    @Override
    @EntityGraph(attributePaths = {"roles", "branches"})
    Optional<UserEntity> findById(String id);

    @EntityGraph(attributePaths = {"roles", "branches"})
    List<UserEntity> findByEmailIgnoreCaseOrderByTenantIdAsc(String email);
}
