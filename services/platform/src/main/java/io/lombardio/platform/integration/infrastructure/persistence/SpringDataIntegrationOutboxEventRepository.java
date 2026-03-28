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
package io.lombardio.platform.integration.infrastructure.persistence;

import io.lombardio.platform.integration.domain.OutboxEventStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataIntegrationOutboxEventRepository
    extends JpaRepository<IntegrationOutboxEventEntity, String> {

  List<IntegrationOutboxEventEntity> findByStatusAndNextAttemptAtLessThanEqualOrderByOccurredAtAsc(
      OutboxEventStatus status, Instant nextAttemptAt, Pageable pageable);
}
