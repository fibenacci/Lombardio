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
package io.lombardio.platform.tenant.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.lombardio.platform.integration.application.PlatformOutboxService;
import io.lombardio.platform.tenant.api.TenantFeatureResponse;
import io.lombardio.platform.tenant.api.UpsertTenantFeatureRequest;
import io.lombardio.platform.tenant.domain.TenantFeature;
import io.lombardio.platform.tenant.domain.TenantFeatureRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantFeatureService {

  public static final Set<String> SUPPORTED_FEATURE_KEYS =
      Set.of(
          "identity-access",
          "customer-management",
          "loan-origination",
          "pawn-ticket-management",
          "aml-compliance",
          "kyc-provider-verification",
          "kyc-document-ocr",
          "auction-workflow",
          "online-auctions",
          "reporting");

  private final TenantFeatureRepository tenantFeatureRepository;
  private final TenantLifecycleService tenantLifecycleService;
  private final PlatformOutboxService platformOutboxService;
  private final ObjectMapper objectMapper;
  private final Clock clock;

  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring managed singleton bean")
  public TenantFeatureService(
      TenantFeatureRepository tenantFeatureRepository,
      TenantLifecycleService tenantLifecycleService,
      PlatformOutboxService platformOutboxService,
      ObjectMapper objectMapper,
      Clock clock) {
    this.tenantFeatureRepository = tenantFeatureRepository;
    this.tenantLifecycleService = tenantLifecycleService;
    this.platformOutboxService = platformOutboxService;
    this.objectMapper = objectMapper;
    this.clock = clock;
  }

  public List<TenantFeatureResponse> listFeatures(String tenantId) {
    tenantLifecycleService.requireTenant(tenantId);
    return tenantFeatureRepository.findByTenantId(tenantId).stream()
        .map(this::toFeatureResponse)
        .toList();
  }

  @Transactional
  public TenantFeatureResponse upsertFeature(
      String tenantId, String featureKey, UpsertTenantFeatureRequest request) {
    tenantLifecycleService.requireTenant(tenantId);
    validateFeatureKey(featureKey);

    TenantFeature feature =
        new TenantFeature(tenantId, featureKey, request.enabled(), Instant.now(clock));

    TenantFeature saved = tenantFeatureRepository.save(feature);
    recordFeatureEvent(
        request.enabled() ? "platform.tenant.feature.enabled" : "platform.tenant.feature.disabled",
        saved);
    return toFeatureResponse(saved);
  }

  private void validateFeatureKey(String featureKey) {
    if (!SUPPORTED_FEATURE_KEYS.contains(featureKey)) {
      throw new IllegalArgumentException("Unsupported feature key: " + featureKey);
    }
  }

  private void recordFeatureEvent(String eventType, TenantFeature feature) {
    platformOutboxService.record(
        "tenant-feature",
        feature.tenantId() + ":" + feature.featureKey(),
        eventType,
        feature.tenantId(),
        toJson(
            Map.of(
                "tenantId",
                feature.tenantId(),
                "featureKey",
                feature.featureKey(),
                "enabled",
                feature.enabled(),
                "updatedAt",
                feature.updatedAt().toString())));
  }

  private String toJson(Object payload) {
    try {
      return objectMapper.writeValueAsString(payload);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Failed to serialize outbox payload", exception);
    }
  }

  private TenantFeatureResponse toFeatureResponse(TenantFeature tenantFeature) {
    return new TenantFeatureResponse(
        tenantFeature.tenantId(),
        tenantFeature.featureKey(),
        tenantFeature.enabled(),
        tenantFeature.updatedAt());
  }
}
