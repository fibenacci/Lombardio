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
import io.lombardio.platform.security.AuthenticatedUser;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class IdentityTenantFeatureDirectory implements TenantFeatureDirectory {

  private static final Logger log = LoggerFactory.getLogger(IdentityTenantFeatureDirectory.class);
  private final RestClient restClient;

  public IdentityTenantFeatureDirectory(
      @Value("${platform.base-url:http://localhost:8082}") String platformBaseUrl,
      RestClient.Builder restClientBuilder) {
    this.restClient = restClientBuilder.baseUrl(platformBaseUrl).build();
  }

  @Override
  public boolean isFeatureEnabled(String tenantId, String featureKey) {
    try {
      String bearerToken = AuthenticatedUser.currentAccessToken().orElse(null);
      if (bearerToken == null) {
        log.warn(
            "[FEATURES] No access token available in security context for tenant {} and feature {}",
            tenantId,
            featureKey);
        return false;
      }

      List<TenantFeatureResponse> features =
          restClient
              .get()
              .uri("/api/v1/tenants/{tenantId}/features", tenantId)
              .headers(headers -> headers.setBearerAuth(bearerToken))
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
      log.warn(
          "[FEATURES] Failed to fetch platform features for tenant {} while checking {}",
          tenantId,
          featureKey,
          e);
      return false;
    }
  }

  private record TenantFeatureResponse(String tenantId, String featureKey, boolean enabled) {}
}
