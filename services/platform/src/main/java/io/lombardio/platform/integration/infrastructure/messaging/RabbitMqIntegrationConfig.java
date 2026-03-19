package io.lombardio.platform.integration.infrastructure.messaging;

import io.lombardio.platform.integration.api.IntegrationRabbitMqProperties;
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
