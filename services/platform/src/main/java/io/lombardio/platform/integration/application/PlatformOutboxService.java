package io.lombardio.platform.integration.application;

import io.lombardio.platform.integration.domain.IntegrationOutboxEvent;
import io.lombardio.platform.integration.domain.IntegrationOutboxEventRepository;
import io.lombardio.platform.integration.domain.OutboxEventStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class PlatformOutboxService {

    private final IntegrationOutboxEventRepository repository;
    private final Clock clock;

    public PlatformOutboxService(IntegrationOutboxEventRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public IntegrationOutboxEvent record(String aggregateType, String aggregateId, String eventType, String tenantId, String payload) {
        Instant now = Instant.now(clock);
        return repository.save(new IntegrationOutboxEvent(
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
                null
        ));
    }

    @Transactional
    public List<OutboxEventResponse> claim(String consumer, int limit) {
        Instant now = Instant.now(clock);
        List<IntegrationOutboxEvent> events = repository.findClaimable(now, limit).stream()
                .map(event -> repository.save(new IntegrationOutboxEvent(
                        event.id(),
                        event.aggregateType(),
                        event.aggregateId(),
                        event.eventType(),
                        event.tenantId(),
                        event.payload(),
                        OutboxEventStatus.PROCESSING,
                        event.attemptCount(),
                        event.occurredAt(),
                        event.nextAttemptAt(),
                        now,
                        consumer,
                        event.publishedAt(),
                        event.lastError()
                )))
                .toList();

        return events.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void complete(String outboxEventId, String consumer) {
        IntegrationOutboxEvent event = requireOwned(outboxEventId, consumer);
        repository.save(new IntegrationOutboxEvent(
                event.id(),
                event.aggregateType(),
                event.aggregateId(),
                event.eventType(),
                event.tenantId(),
                event.payload(),
                OutboxEventStatus.PUBLISHED,
                event.attemptCount(),
                event.occurredAt(),
                event.nextAttemptAt(),
                event.lockedAt(),
                consumer,
                Instant.now(clock),
                null
        ));
    }

    @Transactional
    public void fail(String outboxEventId, String consumer, String errorMessage) {
        IntegrationOutboxEvent event = requireOwned(outboxEventId, consumer);
        int nextAttemptCount = event.attemptCount() + 1;
        long delayMinutes = Math.min(30, 1L << Math.min(nextAttemptCount - 1, 4));
        Instant now = Instant.now(clock);

        repository.save(new IntegrationOutboxEvent(
                event.id(),
                event.aggregateType(),
                event.aggregateId(),
                event.eventType(),
                event.tenantId(),
                event.payload(),
                OutboxEventStatus.PENDING,
                nextAttemptCount,
                event.occurredAt(),
                now.plus(delayMinutes, ChronoUnit.MINUTES),
                null,
                null,
                null,
                errorMessage
        ));
    }

    private IntegrationOutboxEvent requireOwned(String outboxEventId, String consumer) {
        IntegrationOutboxEvent event = repository.findById(outboxEventId)
                .orElseThrow(() -> new IllegalArgumentException("Outbox event not found: " + outboxEventId));

        if (event.status() != OutboxEventStatus.PROCESSING) {
            throw new IllegalArgumentException("Outbox event is not being processed: " + outboxEventId);
        }
        if (event.lockedBy() == null || !event.lockedBy().equals(consumer)) {
            throw new IllegalArgumentException("Outbox event is locked by another consumer: " + outboxEventId);
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
                event.occurredAt()
        );
    }
}
