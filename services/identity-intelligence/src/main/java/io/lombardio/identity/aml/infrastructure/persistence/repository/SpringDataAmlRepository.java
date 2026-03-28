/*
 * Lombardio Source-Available No-Distribution License 1.0
 *
 * Copyright (c) 2026 Benjamin Letzel. All rights reserved.
 *
 * This project is source-available for educational and review purposes only.
 * Redistribution, sublicensing, or commercial use is strictly prohibited.
 *
 * For partnership or cooperation inquiries, please contact the author.
 */
package io.lombardio.identity.aml.infrastructure.persistence.repository;

import io.lombardio.identity.aml.infrastructure.persistence.entity.AmlCaseEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataAmlRepository extends JpaRepository<AmlCaseEntity, String> {

  Optional<AmlCaseEntity> findByTenantIdAndCustomerId(String tenantId, String customerId);
}
