package io.lombardio.identityaccess.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataPermissionRepository extends JpaRepository<PermissionEntity, String> {

    List<PermissionEntity> findAllByOrderByKeyAsc();
}
