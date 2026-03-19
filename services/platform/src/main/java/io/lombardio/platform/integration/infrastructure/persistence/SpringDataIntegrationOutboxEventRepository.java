package io.lombardio.platform.integration.infrastructure.persistence;

import io.lombardio.platform.integration.domain.OutboxEventStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface SpringDataIntegrationOutboxEventRepository extends JpaRepository<IntegrationOutboxEventEntity, String> {

    List<IntegrationOutboxEventEntity> findByStatusAndNextAttemptAtLessThanEqualOrderByOccurredAtAsc(
            OutboxEventStatus status,
            Instant nextAttemptAt,
            Pageable pageable
    );
}
