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

import io.lombardio.platform.integration.domain.IntegrationOutboxEvent;
import io.lombardio.platform.integration.domain.IntegrationOutboxEventRepository;
import io.lombardio.platform.integration.domain.OutboxEventStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
public class IntegrationOutboxPersistenceAdapter implements IntegrationOutboxEventRepository {

  private final SpringDataIntegrationOutboxEventRepository repository;

  public IntegrationOutboxPersistenceAdapter(
      SpringDataIntegrationOutboxEventRepository repository) {
    this.repository = repository;
  }

  @Override
  public IntegrationOutboxEvent save(IntegrationOutboxEvent event) {
    return toDomain(repository.save(toEntity(event)));
  }

  @Override
  public Optional<IntegrationOutboxEvent> findById(String id) {
    return repository.findById(id).map(this::toDomain);
  }

  @Override
  public List<IntegrationOutboxEvent> findClaimable(Instant now, int limit) {
    return repository
        .findByStatusAndNextAttemptAtLessThanEqualOrderByOccurredAtAsc(
            OutboxEventStatus.PENDING, now, PageRequest.of(0, limit))
        .stream()
        .map(this::toDomain)
        .toList();
  }

  private IntegrationOutboxEventEntity toEntity(IntegrationOutboxEvent event) {
    IntegrationOutboxEventEntity entity = new IntegrationOutboxEventEntity();
    entity.setId(event.id());
    entity.setAggregateType(event.aggregateType());
    entity.setAggregateId(event.aggregateId());
    entity.setEventType(event.eventType());
    entity.setTenantId(event.tenantId());
    entity.setPayload(event.payload());
    entity.setStatus(event.status());
    entity.setAttemptCount(event.attemptCount());
    entity.setOccurredAt(event.occurredAt());
    entity.setNextAttemptAt(event.nextAttemptAt());
    entity.setLockedAt(event.lockedAt());
    entity.setLockedBy(event.lockedBy());
    entity.setPublishedAt(event.publishedAt());
    entity.setLastError(event.lastError());
    return entity;
  }

  private IntegrationOutboxEvent toDomain(IntegrationOutboxEventEntity entity) {
    return new IntegrationOutboxEvent(
        entity.getId(),
        entity.getAggregateType(),
        entity.getAggregateId(),
        entity.getEventType(),
        entity.getTenantId(),
        entity.getPayload(),
        entity.getStatus(),
        entity.getAttemptCount(),
        entity.getOccurredAt(),
        entity.getNextAttemptAt(),
        entity.getLockedAt(),
        entity.getLockedBy(),
        entity.getPublishedAt(),
        entity.getLastError());
  }
}
