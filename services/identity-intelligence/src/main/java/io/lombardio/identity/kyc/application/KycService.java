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
package io.lombardio.identity.kyc.application;

import io.lombardio.identity.domain.port.TenantFeatureDirectory;
import io.lombardio.identity.kyc.api.DocumentPrefillRequest;
import io.lombardio.identity.kyc.api.DocumentPrefillResponse;
import io.lombardio.identity.kyc.api.KycDocumentImagesResponse;
import io.lombardio.identity.kyc.api.KycStatusResponse;
import io.lombardio.identity.kyc.api.UpdateKycStatusRequest;
import io.lombardio.identity.kyc.domain.DocumentOcrProvider;
import io.lombardio.identity.kyc.domain.KycRecord;
import io.lombardio.identity.kyc.domain.KycRepository;
import io.lombardio.identity.kyc.domain.KycStatus;
import io.lombardio.identity.kyc.domain.KycVerificationMode;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class KycService {

  public static final String PROVIDER_FEATURE_KEY = "kyc-provider-verification";
  public static final String OCR_FEATURE_KEY = "kyc-document-ocr";

  private final KycRepository kycRepository;
  private final TenantFeatureDirectory tenantFeatureDirectory;
  private final DocumentOcrProvider documentOcrProvider;
  private final KycMetrics metrics;

  public KycService(
      KycRepository kycRepository,
      TenantFeatureDirectory tenantFeatureDirectory,
      DocumentOcrProvider documentOcrProvider) {
    this(kycRepository, tenantFeatureDirectory, documentOcrProvider, KycMetrics.noop());
  }

  @Autowired
  public KycService(
      KycRepository kycRepository,
      TenantFeatureDirectory tenantFeatureDirectory,
      DocumentOcrProvider documentOcrProvider,
      MeterRegistry meterRegistry) {
    this(kycRepository, tenantFeatureDirectory, documentOcrProvider, new KycMetrics(meterRegistry));
  }

  private KycService(
      KycRepository kycRepository,
      TenantFeatureDirectory tenantFeatureDirectory,
      DocumentOcrProvider documentOcrProvider,
      KycMetrics metrics) {
    this.kycRepository = kycRepository;
    this.tenantFeatureDirectory = tenantFeatureDirectory;
    this.documentOcrProvider = documentOcrProvider;
    this.metrics = metrics;
  }

  public KycStatusResponse getStatus(String tenantId, String customerId) {
    KycRecord record =
        kycRepository
            .findByTenantIdAndCustomerId(tenantId, customerId)
            .orElseGet(() -> KycRecordFactory.empty(tenantId, customerId));
    return toResponse(record, providerVerificationAvailable(tenantId));
  }

  public KycDocumentImagesResponse getDocumentImages(String tenantId, String customerId) {
    KycRecord record =
        kycRepository
            .findByTenantIdAndCustomerId(tenantId, customerId)
            .orElseGet(() -> KycRecordFactory.empty(tenantId, customerId));

    return new KycDocumentImagesResponse(
        record.customerId(),
        DocumentImageDataUrlNormalizer.normalize(record.documentFrontImageDataUrl()),
        DocumentImageDataUrlNormalizer.normalize(record.documentBackImageDataUrl()));
  }

  public KycStatusResponse updateStatus(
      String tenantId, String customerId, UpdateKycStatusRequest request) {
    KycVerificationMode verificationMode =
        request.verificationMode() == null
            ? KycVerificationMode.MANUAL
            : request.verificationMode();

    if (verificationMode == KycVerificationMode.PROVIDER
        && !providerVerificationAvailable(tenantId)) {
      throw new IllegalArgumentException(
          "Provider verification is not enabled for tenant: " + tenantId);
    }

    KycRecord existing =
        kycRepository
            .findByTenantIdAndCustomerId(tenantId, customerId)
            .orElse(KycRecordFactory.empty(tenantId, customerId));

    KycValidationRules.validateManualDocumentData(request, verificationMode);

    KycRecord updated =
        KycRecordFactory.update(existing, tenantId, customerId, verificationMode, request);

    KycStatusResponse response =
        toResponse(kycRepository.save(updated), providerVerificationAvailable(tenantId));
    metrics.recordStatusUpdate(updated.status(), updated.verificationMode());
    return response;
  }

  public boolean isApproved(String tenantId, String customerId) {
    return kycRepository
        .findByTenantIdAndCustomerId(tenantId, customerId)
        .filter(record -> record.status() == KycStatus.APPROVED)
        .filter(
            record ->
                record.verifiedUntil() == null || !record.verifiedUntil().isBefore(LocalDate.now()))
        .isPresent();
  }

  public DocumentPrefillResponse prefillDocumentData(
      String tenantId, DocumentPrefillRequest request) {
    if (!tenantFeatureDirectory.isFeatureEnabled(tenantId, OCR_FEATURE_KEY)) {
      return new DocumentPrefillResponse(
          false, false, null, null, null, null, null, null, null, null, 0);
    }

    DocumentPrefillResponse response =
        documentOcrProvider
            .prefill(
                tenantId,
                DocumentImageDataUrlNormalizer.normalize(request.documentFrontImageDataUrl()),
                DocumentImageDataUrlNormalizer.normalize(request.documentBackImageDataUrl()))
            .map(
                result ->
                    new DocumentPrefillResponse(
                        true,
                        true,
                        result.firstName(),
                        result.lastName(),
                        result.birthDate(),
                        result.documentType(),
                        result.documentNumber(),
                        result.documentValidUntil(),
                        result.portraitImageDataUrl(),
                        result.providerName(),
                        result.confidence()))
            .orElseGet(
                () ->
                    new DocumentPrefillResponse(
                        true, false, null, null, null, null, null, null, null, null, 0));
    metrics.recordDocumentPrefill(response.matched(), response.providerName());
    return response;
  }

  private boolean providerVerificationAvailable(String tenantId) {
    return tenantFeatureDirectory.isFeatureEnabled(tenantId, PROVIDER_FEATURE_KEY);
  }

  private KycStatusResponse toResponse(KycRecord record, boolean providerVerificationAvailable) {
    return new KycStatusResponse(
        record.customerId(),
        record.verificationMode(),
        record.status(),
        record.verifiedUntil(),
        record.documentType(),
        record.documentNumber(),
        record.documentValidUntil(),
        record.decisionNote(),
        record.providerName(),
        record.providerReference(),
        record.providerStatus(),
        providerVerificationAvailable);
  }
}
