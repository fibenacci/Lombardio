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
package io.lombardio.platform.integration.application;

import io.lombardio.platform.integration.domain.IntegrationOutboxEvent;
import io.lombardio.platform.integration.domain.IntegrationOutboxEventRepository;
import io.lombardio.platform.integration.domain.OutboxEventStatus;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlatformOutboxService {

  private final IntegrationOutboxEventRepository repository;
  private final Clock clock;

  public PlatformOutboxService(IntegrationOutboxEventRepository repository, Clock clock) {
    this.repository = repository;
    this.clock = clock;
  }

  public IntegrationOutboxEvent record(
      String aggregateType, String aggregateId, String eventType, String tenantId, String payload) {
    return repository.save(
        IntegrationOutboxEvent.create(
            aggregateType, aggregateId, eventType, tenantId, payload, Instant.now(clock)));
  }

  @Transactional
  public List<OutboxEventResponse> claim(String consumer, int limit) {
    Instant now = Instant.now(clock);
    List<IntegrationOutboxEvent> events =
        repository.findClaimable(now, limit).stream()
            .map(event -> repository.save(event.claim(consumer, now)))
            .toList();

    return events.stream().map(this::toResponse).toList();
  }

  @Transactional
  public void complete(String outboxEventId, String consumer) {
    IntegrationOutboxEvent event = requireOwned(outboxEventId, consumer);
    repository.save(event.complete(Instant.now(clock)));
  }

  @Transactional
  public void fail(String outboxEventId, String consumer, String errorMessage) {
    IntegrationOutboxEvent event = requireOwned(outboxEventId, consumer);
    repository.save(event.fail(errorMessage, Instant.now(clock)));
  }

  private IntegrationOutboxEvent requireOwned(String outboxEventId, String consumer) {
    IntegrationOutboxEvent event =
        repository
            .findById(outboxEventId)
            .orElseThrow(
                () -> new IllegalArgumentException("Outbox event not found: " + outboxEventId));

    if (event.status() != OutboxEventStatus.PROCESSING) {
      throw new IllegalArgumentException("Outbox event is not being processed: " + outboxEventId);
    }
    if (event.lockedBy() == null || !event.lockedBy().equals(consumer)) {
      throw new IllegalArgumentException(
          "Outbox event is locked by another consumer: " + outboxEventId);
    }
    return event;
  }

  private OutboxEventResponse toResponse(IntegrationOutboxEvent event) {
    return new OutboxEventResponse(
        event.id(),
        event.aggregateType(),
        event.aggregateId(),
        event.eventType(),
        event.tenantId(),
        event.payload(),
        event.occurredAt());
  }
}
