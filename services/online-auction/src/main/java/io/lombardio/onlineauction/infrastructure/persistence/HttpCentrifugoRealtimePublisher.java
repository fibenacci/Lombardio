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
package io.lombardio.onlineauction.infrastructure.persistence;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.lombardio.onlineauction.config.CentrifugoProperties;
import io.lombardio.onlineauction.domain.RealtimePublisher;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class HttpCentrifugoRealtimePublisher implements RealtimePublisher {

  private static final Logger log = LoggerFactory.getLogger(HttpCentrifugoRealtimePublisher.class);
  private final RestClient restClient;
  private final CentrifugoProperties properties;

  public HttpCentrifugoRealtimePublisher(CentrifugoProperties properties) {
    this.properties = properties;
    this.restClient = RestClient.builder().baseUrl(properties.baseUrl()).build();
  }

  @Override
  @SuppressFBWarnings(
      value = "CRLF_INJECTION_LOGS",
      justification = "Channel and exception message are sanitized before logging")
  public void publish(String channel, Object payload) {
    try {
      restClient
          .post()
          .uri("/api/publish")
          .header("X-API-Key", properties.apiKey())
          .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
          .body(Map.of("channel", channel, "data", payload))
          .retrieve()
          .toBodilessEntity();
    } catch (Exception exception) {
      String sanitizedChannel = sanitizeForLogs(channel);
      String sanitizedMessage = sanitizeForLogs(exception.getMessage());
      log.warn(
          "[REALTIME] Failed to publish event to channel {}: {}",
          sanitizedChannel,
          sanitizedMessage);
    }
  }

  private String sanitizeForLogs(String value) {
    if (value == null) {
      return "null";
    }
    return value.replace('\r', '_').replace('\n', '_').replace('\t', '_');
  }
}
