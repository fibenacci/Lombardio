package io.lombardio.aml.infrastructure.persistence.repository;

import io.lombardio.aml.infrastructure.persistence.entity.AmlCaseEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataAmlRepository extends JpaRepository<AmlCaseEntity, String> {

    Optional<AmlCaseEntity> findByTenantIdAndCustomerId(String tenantId, String customerId);
}
