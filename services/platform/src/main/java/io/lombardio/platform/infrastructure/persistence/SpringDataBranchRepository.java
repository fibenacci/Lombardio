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
package io.lombardio.platform.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataBranchRepository extends JpaRepository<BranchEntity, String> {

  List<BranchEntity> findByTenantIdOrderByCreatedAtAsc(String tenantId);

  Optional<BranchEntity> findByTenantIdAndKey(String tenantId, String key);
}
