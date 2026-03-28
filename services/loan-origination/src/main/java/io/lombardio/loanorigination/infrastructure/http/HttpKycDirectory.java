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
package io.lombardio.loanorigination.infrastructure.http;

import io.lombardio.loanorigination.domain.port.KycDirectory;
import io.lombardio.platform.security.AuthenticatedUser;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class HttpKycDirectory implements KycDirectory {

  private final RestClient restClient;

  public HttpKycDirectory(
      @Value("${identity.base-url:http://localhost:8084}") String baseUrl,
      RestClient.Builder restClientBuilder) {
    this.restClient = restClientBuilder.baseUrl(baseUrl).build();
  }

  @Override
  public KycProjection getStatus(String tenantId, String customerId) {
    return getStatus(tenantId, customerId, AuthenticatedUser.currentAccessToken());
  }

  @Override
  public KycProjection getStatus(String tenantId, String customerId, Optional<String> accessToken) {
    try {
      KycRecord record =
          restClient
              .get()
              .uri("/api/v1/tenants/{tenantId}/customers/{customerId}/kyc", tenantId, customerId)
              .headers(
                  headers -> {
                    accessToken.ifPresent(
                        token -> {
                          System.out.println(
                              "[DEBUG] Explicitly adding token to KYC headers for " + customerId);
                          headers.setBearerAuth(token);
                        });
                  })
              .retrieve()
              .body(KycRecord.class);

      if (record == null) {
        return new KycProjection("NOT_STARTED", false, null);
      }
      return new KycProjection(
          record.status(), "APPROVED".equals(record.status()), record.documentType());
    } catch (Exception e) {
      System.err.println(
          "[ERROR] Failed to fetch KYC status for customer " + customerId + ": " + e.getMessage());
      return new KycProjection("UNKNOWN", false, null);
    }
  }

  @Override
  public boolean isApproved(String tenantId, String customerId) {
    return getStatus(tenantId, customerId).approved();
  }

  private record KycRecord(String customerId, String status, String documentType) {}
}
