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
    String lastError) {}
