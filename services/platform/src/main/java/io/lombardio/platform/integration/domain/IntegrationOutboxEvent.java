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
import java.time.temporal.ChronoUnit;
import java.util.UUID;

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
    String lastError) {

  public static IntegrationOutboxEvent create(
      String aggregateType,
      String aggregateId,
      String eventType,
      String tenantId,
      String payload,
      Instant now) {
    return new IntegrationOutboxEvent(
        "outbox-" + UUID.randomUUID(),
        aggregateType,
        aggregateId,
        eventType,
        tenantId,
        payload,
        OutboxEventStatus.PENDING,
        0,
        now,
        now,
        null,
        null,
        null,
        null);
  }

  public IntegrationOutboxEvent claim(String consumer, Instant now) {
    return new IntegrationOutboxEvent(
        id,
        aggregateType,
        aggregateId,
        eventType,
        tenantId,
        payload,
        OutboxEventStatus.PROCESSING,
        attemptCount,
        occurredAt,
        nextAttemptAt,
        now,
        consumer,
        publishedAt,
        lastError);
  }

  public IntegrationOutboxEvent complete(Instant now) {
    return new IntegrationOutboxEvent(
        id,
        aggregateType,
        aggregateId,
        eventType,
        tenantId,
        payload,
        OutboxEventStatus.PUBLISHED,
        attemptCount,
        occurredAt,
        nextAttemptAt,
        lockedAt,
        lockedBy,
        now,
        null);
  }

  public IntegrationOutboxEvent fail(String errorMessage, Instant now) {
    int nextAttemptCount = attemptCount + 1;
    long delayMinutes = Math.min(30, 1L << Math.min(nextAttemptCount - 1, 4));
    return new IntegrationOutboxEvent(
        id,
        aggregateType,
        aggregateId,
        eventType,
        tenantId,
        payload,
        OutboxEventStatus.PENDING,
        nextAttemptCount,
        occurredAt,
        now.plus(delayMinutes, ChronoUnit.MINUTES),
        null,
        null,
        null,
        errorMessage);
  }
}
