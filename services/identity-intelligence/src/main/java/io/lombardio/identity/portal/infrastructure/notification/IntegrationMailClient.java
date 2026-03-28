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
package io.lombardio.identity.portal.infrastructure.notification;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class IntegrationMailClient {

  private static final String INTERNAL_SERVICE_HEADER = "X-Internal-Service-Token";

  private final RestClient restClient;
  private final String internalServiceToken;

  public IntegrationMailClient(
      RestClient.Builder restClientBuilder,
      @Value("${integration.base-url:http://localhost:8092}") String integrationBaseUrl,
      @Value("${customer.internal-service-token:dev-internal-token}") String internalServiceToken) {
    this.restClient = restClientBuilder.baseUrl(integrationBaseUrl).build();
    this.internalServiceToken = internalServiceToken;
  }

  public void send(
      String tenantId,
      List<String> recipients,
      String subject,
      String textBody,
      String htmlBody,
      Map<String, String> metadata) {
    restClient
        .post()
        .uri("/internal/v1/emails/send")
        .header(INTERNAL_SERVICE_HEADER, internalServiceToken)
        .header(HttpHeaders.CONTENT_TYPE, "application/json")
        .body(
            new IntegrationMailRequest(
                tenantId, recipients, List.of(), subject, textBody, htmlBody, metadata))
        .retrieve()
        .toBodilessEntity();
  }

  private record IntegrationMailRequest(
      String tenantId,
      List<String> to,
      List<String> replyTo,
      String subject,
      String textBody,
      String htmlBody,
      Map<String, String> metadata) {}
}
