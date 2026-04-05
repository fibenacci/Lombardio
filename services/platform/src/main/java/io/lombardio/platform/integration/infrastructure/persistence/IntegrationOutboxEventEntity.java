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
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "integration_outbox_events")
public class IntegrationOutboxEventEntity {

  @Id private String id;

  @Column(name = "aggregate_type", nullable = false)
  private String aggregateType;

  @Column(name = "aggregate_id", nullable = false)
  private String aggregateId;

  @Column(name = "event_type", nullable = false)
  private String eventType;

  @Column(name = "tenant_id", nullable = false)
  private String tenantId;

  @Column(name = "payload", nullable = false, columnDefinition = "text")
  private String payload;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private OutboxEventStatus status;

  @Column(name = "attempt_count", nullable = false)
  private int attemptCount;

  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt;

  @Column(name = "next_attempt_at", nullable = false)
  private Instant nextAttemptAt;

  @Column(name = "locked_at")
  private Instant lockedAt;

  @Column(name = "locked_by")
  private String lockedBy;

  @Column(name = "published_at")
  private Instant publishedAt;

  @Column(name = "last_error")
  private String lastError;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getAggregateType() {
    return aggregateType;
  }

  public void setAggregateType(@NotNull String aggregateType) {
    this.aggregateType = Objects.requireNonNull(aggregateType, "aggregateType");
  }

  public String getAggregateId() {
    return aggregateId;
  }

  public void setAggregateId(@NotNull String aggregateId) {
    this.aggregateId = Objects.requireNonNull(aggregateId, "aggregateId");
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(@NotNull String eventType) {
    this.eventType = Objects.requireNonNull(eventType, "eventType");
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(@NotNull String tenantId) {
    this.tenantId = Objects.requireNonNull(tenantId, "tenantId");
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(@NotNull String payload) {
    this.payload = Objects.requireNonNull(payload, "payload");
  }

  public OutboxEventStatus getStatus() {
    return status;
  }

  public void setStatus(@NotNull OutboxEventStatus status) {
    this.status = Objects.requireNonNull(status, "status");
  }

  public int getAttemptCount() {
    return attemptCount;
  }

  public void setAttemptCount(int attemptCount) {
    this.attemptCount = attemptCount;
  }

  public Instant getOccurredAt() {
    return occurredAt;
  }

  public void setOccurredAt(@NotNull Instant occurredAt) {
    this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt");
  }

  public Instant getNextAttemptAt() {
    return nextAttemptAt;
  }

  public void setNextAttemptAt(@NotNull Instant nextAttemptAt) {
    this.nextAttemptAt = Objects.requireNonNull(nextAttemptAt, "nextAttemptAt");
  }

  public Instant getLockedAt() {
    return lockedAt;
  }

  public void setLockedAt(Instant lockedAt) {
    this.lockedAt = lockedAt;
  }

  public String getLockedBy() {
    return lockedBy;
  }

  public void setLockedBy(String lockedBy) {
    this.lockedBy = lockedBy;
  }

  public Instant getPublishedAt() {
    return publishedAt;
  }

  public void setPublishedAt(Instant publishedAt) {
    this.publishedAt = publishedAt;
  }

  public String getLastError() {
    return lastError;
  }

  public void setLastError(String lastError) {
    this.lastError = lastError;
  }
}
