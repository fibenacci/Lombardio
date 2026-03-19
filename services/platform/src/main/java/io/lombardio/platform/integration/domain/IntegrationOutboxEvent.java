package io.lombardio.platform.integration.domain;

import java.time.Instant;

public record IntegrationOutboxEvent(
        String id,
        String aggregateType,
        String aggregateId,
        String eventType,
        String tenantId,
        String payload,
        OutboxEventStatus status,
        int attemptCount,
        Instant occurredAt,
        Instant nextAttemptAt,
        Instant lockedAt,
        String lockedBy,
        Instant publishedAt,
        String lastError
) {
}
