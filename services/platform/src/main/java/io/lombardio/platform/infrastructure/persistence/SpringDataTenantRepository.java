package io.lombardio.platform.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataTenantRepository extends JpaRepository<TenantEntity, String> {

    Optional<TenantEntity> findByKey(String key);
}
