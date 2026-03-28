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
package io.lombardio.identity.infrastructure.platform;

import io.lombardio.identity.domain.port.TenantFeatureDirectory;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class IdentityTenantFeatureDirectory implements TenantFeatureDirectory {

  private final RestClient restClient;

  public IdentityTenantFeatureDirectory(
      @Value("${platform.base-url:http://localhost:8082}") String platformBaseUrl,
      RestClient.Builder restClientBuilder) {
    this.restClient = restClientBuilder.baseUrl(platformBaseUrl).build();
  }

  @Override
  public boolean isFeatureEnabled(String tenantId, String featureKey) {
    try {
      // Wir fragen alle Features des Mandanten ab
      List<TenantFeatureResponse> features =
          restClient
              .get()
              .uri("/api/v1/platform/tenants/{tenantId}/features", tenantId)
              .retrieve()
              .body(new ParameterizedTypeReference<List<TenantFeatureResponse>>() {});

      if (features == null) {
        return false;
      }

      return features.stream()
          .filter(f -> f.featureKey().equals(featureKey))
          .map(TenantFeatureResponse::enabled)
          .findFirst()
          .orElse(false);
    } catch (Exception e) {
      System.err.println(
          "[ERROR] Failed to fetch features from platform service for tenant "
              + tenantId
              + ": "
              + e.getMessage());
      return false;
    }
  }

  private record TenantFeatureResponse(String tenantId, String featureKey, boolean enabled) {}
}
