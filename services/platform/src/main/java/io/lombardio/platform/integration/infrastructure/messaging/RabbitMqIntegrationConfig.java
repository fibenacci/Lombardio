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
package io.lombardio.platform.integration.infrastructure.messaging;

import io.lombardio.platform.config.IntegrationRabbitMqProperties;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqIntegrationConfig {

  @Bean
  @ConditionalOnProperty(name = "integration.rabbitmq.publisher-enabled", havingValue = "true")
  Declarables integrationRabbitMqDeclarables(IntegrationRabbitMqProperties properties) {
    return new Declarables(new TopicExchange(properties.exchange(), true, false));
  }
}
