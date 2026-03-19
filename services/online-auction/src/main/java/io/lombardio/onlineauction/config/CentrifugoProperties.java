package io.lombardio.onlineauction.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "centrifugo")
public record CentrifugoProperties(
        String baseUrl,
        String apiKey,
        String hmacSecret,
        String wsUrl
) {
}
