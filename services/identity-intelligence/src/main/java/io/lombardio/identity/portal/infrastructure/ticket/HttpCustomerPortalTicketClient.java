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
package io.lombardio.identity.portal.infrastructure.ticket;

import io.lombardio.identity.portal.api.CustomerPortalPawnTicketResponse;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class HttpCustomerPortalTicketClient implements CustomerPortalTicketClient {

  private static final String INTERNAL_AUTH_HEADER = "X-Internal-Service-Token";

  private final RestClient restClient;
  private final String internalServiceToken;

  public HttpCustomerPortalTicketClient(
      RestClient.Builder restClientBuilder,
      @Value("${pawn-ticket.base-url:http://localhost:8085}") String pawnTicketBaseUrl,
      @Value("${customer.internal-service-token}") String internalServiceToken) {
    this.restClient = restClientBuilder.baseUrl(pawnTicketBaseUrl).build();
    this.internalServiceToken = requireSecureToken(internalServiceToken);
  }

  @Override
  public List<CustomerPortalPawnTicketResponse> listTickets(String tenantId, String customerId) {
    try {
      CustomerPortalPawnTicketResponse[] response =
          restClient
              .get()
              .uri(
                  "/api/internal/v1/customers/{tenantId}/{customerId}/pawn-tickets",
                  tenantId,
                  customerId)
              .header(INTERNAL_AUTH_HEADER, internalServiceToken)
              .retrieve()
              .body(CustomerPortalPawnTicketResponse[].class);
      return response == null ? List.of() : Arrays.asList(response);
    } catch (RestClientException exception) {
      throw new IllegalStateException("Pawn ticket service unavailable", exception);
    }
  }

  @Override
  public byte[] downloadDocument(String tenantId, String customerId, String ticketNumber) {
    try {
      return restClient
          .get()
          .uri(
              "/api/internal/v1/customers/{tenantId}/{customerId}/pawn-tickets/{ticketNumber}/document",
              tenantId,
              customerId,
              ticketNumber)
          .header(INTERNAL_AUTH_HEADER, internalServiceToken)
          .header(HttpHeaders.ACCEPT, "application/pdf")
          .retrieve()
          .body(byte[].class);
    } catch (RestClientException exception) {
      throw new IllegalStateException("Pawn ticket service unavailable", exception);
    }
  }

  private static String requireSecureToken(String token) {
    if (token == null
        || token.isBlank()
        || "REPLACE_WITH_SECURE_TOKEN".equals(token)
        || "dev-internal-token".equals(token)) {
      throw new IllegalStateException(
          "customer.internal-service-token must be configured with a secure value");
    }
    return token;
  }
}
