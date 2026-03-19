package io.lombardio.platform.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataTenantFeatureRepository extends JpaRepository<TenantFeatureEntity, TenantFeatureId> {

    List<TenantFeatureEntity> findByIdTenantIdOrderByIdFeatureKey(String tenantId);
}
