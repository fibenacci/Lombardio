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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lombardio.platform.integration.api.IntegrationRabbitMqProperties;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "integration.rabbitmq.publisher-enabled", havingValue = "true")
public class PlatformOutboxPublisher {

  private static final String PUBLISHER_CONSUMER = "platform-rabbitmq-publisher";

  private final PlatformOutboxService platformOutboxService;
  private final IntegrationRabbitMqProperties properties;
  private final RabbitTemplate rabbitTemplate;
  private final ObjectMapper objectMapper;

  public PlatformOutboxPublisher(
      PlatformOutboxService platformOutboxService,
      IntegrationRabbitMqProperties properties,
      RabbitTemplate rabbitTemplate,
      ObjectMapper objectMapper) {
    this.platformOutboxService = platformOutboxService;
    this.properties = properties;
    this.rabbitTemplate = rabbitTemplate;
    this.objectMapper = objectMapper;
  }

  @Scheduled(fixedDelay = 5000L)
  public void publishPendingEvents() {
    for (OutboxEventResponse event :
        platformOutboxService.claim(PUBLISHER_CONSUMER, properties.publisherBatchSize())) {
      try {
        rabbitTemplate.convertAndSend(
            properties.exchange(),
            event.eventType(),
            objectMapper.writeValueAsString(toEnvelope(event)));
        platformOutboxService.complete(event.id(), PUBLISHER_CONSUMER);
      } catch (Exception exception) {
        platformOutboxService.fail(event.id(), PUBLISHER_CONSUMER, exception.getMessage());
      }
    }
  }

  private Map<String, Object> toEnvelope(OutboxEventResponse event) throws Exception {
    Map<String, Object> envelope = new LinkedHashMap<>();
    envelope.put("id", event.id());
    envelope.put("aggregateType", event.aggregateType());
    envelope.put("aggregateId", event.aggregateId());
    envelope.put("eventType", event.eventType());
    envelope.put("tenantId", event.tenantId());
    envelope.put("occurredAt", event.occurredAt().toString());
    envelope.put("payload", objectMapper.readTree(event.payload()));
    return envelope;
  }
}
