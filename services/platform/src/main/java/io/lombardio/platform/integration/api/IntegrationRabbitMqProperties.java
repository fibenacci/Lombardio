package io.lombardio.platform.integration.api;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.rabbitmq")
public record IntegrationRabbitMqProperties(
        String exchange,
        int publisherBatchSize,
        boolean publisherEnabled
) {
}
