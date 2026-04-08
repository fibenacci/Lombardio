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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lombardio.platform.integration.api.IntegrationRabbitMqProperties;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

class PlatformOutboxPublisherTest {

  private PlatformOutboxService outboxService;
  private RabbitTemplate rabbitTemplate;
  private PlatformOutboxPublisher publisher;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    outboxService = mock(PlatformOutboxService.class);
    rabbitTemplate = mock(RabbitTemplate.class);
    IntegrationRabbitMqProperties properties =
        new IntegrationRabbitMqProperties("platform.exchange", 10, true);

    publisher =
        new PlatformOutboxPublisher(outboxService, properties, rabbitTemplate, objectMapper);
  }

  @Test
  void publishesPendingEvents() {
    OutboxEventResponse event =
        new OutboxEventResponse(
            "event-1",
            "TENANT",
            "tenant-1",
            "tenant_created",
            "tenant-1",
            "{\"name\":\"Test Tenant\"}",
            Instant.now());

    when(outboxService.claim(anyString(), anyInt())).thenReturn(List.of(event));

    publisher.publishPendingEvents();

    verify(rabbitTemplate)
        .convertAndSend(eq("platform.exchange"), eq("tenant_created"), anyString());
    verify(outboxService).complete(eq("event-1"), anyString());
  }

  @Test
  void failsEventOnPublishError() {
    OutboxEventResponse event =
        new OutboxEventResponse(
            "event-1",
            "TENANT",
            "tenant-1",
            "tenant_created",
            "tenant-1",
            "{\"name\":\"Test Tenant\"}",
            Instant.now());

    when(outboxService.claim(anyString(), anyInt())).thenReturn(List.of(event));
    doThrow(new RuntimeException("Rabbit error"))
        .when(rabbitTemplate)
        .convertAndSend(anyString(), anyString(), anyString());

    publisher.publishPendingEvents();

    verify(outboxService).fail(eq("event-1"), anyString(), eq("Rabbit error"));
    verify(outboxService, never()).complete(anyString(), anyString());
  }
}
