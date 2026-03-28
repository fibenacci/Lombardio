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
package io.lombardio.identity.aml.application.service;

import io.lombardio.identity.aml.api.http.AmlStatusResponse;
import io.lombardio.identity.aml.api.http.OriginationAssessmentRequest;
import io.lombardio.identity.aml.api.http.UpdateAmlStatusRequest;
import io.lombardio.identity.aml.domain.model.AmlCase;
import io.lombardio.identity.aml.domain.model.AmlRiskLevel;
import io.lombardio.identity.aml.domain.model.AmlStatus;
import io.lombardio.identity.aml.domain.port.AmlRepository;
import io.lombardio.identity.domain.port.TenantFeatureDirectory;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AmlService {

  public static final String AML_FEATURE_KEY = "aml-compliance";

  private final AmlRepository amlRepository;
  private final TenantFeatureDirectory tenantFeatureDirectory;
  private final Clock clock;

  public AmlService(
      AmlRepository amlRepository, TenantFeatureDirectory tenantFeatureDirectory, Clock clock) {
    this.amlRepository = amlRepository;
    this.tenantFeatureDirectory = tenantFeatureDirectory;
    this.clock = clock;
  }

  public AmlStatusResponse getStatus(String tenantId, String customerId) {
    AmlCase amlCase =
        amlRepository
            .findByTenantIdAndCustomerId(tenantId, customerId)
            .orElseGet(() -> defaultCase(tenantId, customerId));
    Assessment assessment = assess(tenantId, amlCase, null);
    return toResponse(amlCase, assessment);
  }

  public AmlStatusResponse updateStatus(
      String tenantId, String customerId, UpdateAmlStatusRequest request) {
    AmlCase existing =
        amlRepository
            .findByTenantIdAndCustomerId(tenantId, customerId)
            .orElseGet(() -> defaultCase(tenantId, customerId));

    if (request.suspiciousActivityReported() && isBlank(request.goamlReference())) {
      throw new IllegalArgumentException(
          "goamlReference is required when suspiciousActivityReported is true");
    }

    Instant now = Instant.now(clock);
    AmlCase updated =
        new AmlCase(
            existing.id(),
            tenantId,
            customerId,
            request.status(),
            request.riskLevel(),
            request.pepFlag(),
            request.sanctionsHit(),
            request.unusualTransactionFlag(),
            request.sourceOfFundsChecked(),
            request.suspiciousActivityReported(),
            request.goamlReference(),
            request.decisionNote(),
            request.lastScreenedAt(),
            request.reviewedAt() == null ? now : request.reviewedAt(),
            now);

    Assessment assessment = assess(tenantId, updated, null);
    return toResponse(amlRepository.save(updated), assessment);
  }

  public AmlStatusResponse assessForOrigination(
      String tenantId, String customerId, OriginationAssessmentRequest request) {
    AmlCase amlCase =
        amlRepository
            .findByTenantIdAndCustomerId(tenantId, customerId)
            .orElseGet(() -> defaultCase(tenantId, customerId));
    return toResponse(amlCase, assess(tenantId, amlCase, request.loanAmount()));
  }

  private Assessment assess(String tenantId, AmlCase amlCase, BigDecimal loanAmount) {
    boolean featureEnabled = tenantFeatureDirectory.isFeatureEnabled(tenantId, AML_FEATURE_KEY);
    if (!featureEnabled) {
      return new Assessment(false, true, "AML compliance feature disabled for tenant");
    }

    if (amlCase.suspiciousActivityReported() || amlCase.status() == AmlStatus.REPORTED) {
      return new Assessment(true, false, "Suspicious activity case already reported");
    }
    if (amlCase.sanctionsHit() || amlCase.status() == AmlStatus.BLOCKED) {
      return new Assessment(true, false, "Sanctions or blocking decision prevents origination");
    }
    if (amlCase.status() != AmlStatus.CLEAR) {
      return new Assessment(true, false, "AML review required before loan origination");
    }
    if (amlCase.pepFlag()
        && amlCase.riskLevel() == AmlRiskLevel.HIGH
        && !amlCase.sourceOfFundsChecked()) {
      return new Assessment(true, false, "Enhanced due diligence required for high-risk PEP");
    }
    if (loanAmount != null
        && loanAmount.compareTo(new BigDecimal("2000.00")) >= 0
        && !amlCase.sourceOfFundsChecked()) {
      return new Assessment(
          true, false, "Source of funds review required for higher-value origination");
    }
    if (amlCase.unusualTransactionFlag()) {
      return new Assessment(true, false, "Unusual transaction flag requires manual AML review");
    }

    return new Assessment(true, true, "AML review cleared for origination");
  }

  private AmlCase defaultCase(String tenantId, String customerId) {
    Instant now = Instant.now(clock);
    return new AmlCase(
        "aml-" + UUID.randomUUID(),
        tenantId,
        customerId,
        AmlStatus.NOT_REVIEWED,
        AmlRiskLevel.MEDIUM,
        false,
        false,
        false,
        false,
        false,
        null,
        null,
        null,
        null,
        now);
  }

  private AmlStatusResponse toResponse(AmlCase amlCase, Assessment assessment) {
    return new AmlStatusResponse(
        amlCase.customerId(),
        amlCase.status(),
        amlCase.riskLevel(),
        amlCase.pepFlag(),
        amlCase.sanctionsHit(),
        amlCase.unusualTransactionFlag(),
        amlCase.sourceOfFundsChecked(),
        amlCase.suspiciousActivityReported(),
        amlCase.goamlReference(),
        amlCase.decisionNote(),
        amlCase.lastScreenedAt(),
        amlCase.reviewedAt(),
        assessment.featureAvailable(),
        assessment.originationAllowed(),
        assessment.decisionReason());
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }

  private record Assessment(
      boolean featureAvailable, boolean originationAllowed, String decisionReason) {}
}
