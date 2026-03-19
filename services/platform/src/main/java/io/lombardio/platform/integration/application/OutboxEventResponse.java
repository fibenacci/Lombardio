package io.lombardio.platform.integration.application;

import java.time.Instant;

public record OutboxEventResponse(
        String id,
        String aggregateType,
        String aggregateId,
        String eventType,
        String tenantId,
        String payload,
        Instant occurredAt
) {
}
