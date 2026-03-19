package io.lombardio.platform.integration.api;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.outbox")
public record IntegrationOutboxProperties(
        String accessToken
) {
}
