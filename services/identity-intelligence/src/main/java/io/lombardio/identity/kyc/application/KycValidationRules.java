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

import io.lombardio.identity.kyc.api.UpdateKycStatusRequest;
import io.lombardio.identity.kyc.domain.KycStatus;
import io.lombardio.identity.kyc.domain.KycVerificationMode;
import java.time.LocalDate;

final class KycValidationRules {

  private KycValidationRules() {}

  static void validateManualDocumentData(
      UpdateKycStatusRequest request, KycVerificationMode verificationMode) {
    if (verificationMode != KycVerificationMode.MANUAL) {
      return;
    }
    if (request.status() != KycStatus.APPROVED && request.status() != KycStatus.IN_PROGRESS) {
      return;
    }
    requireValue(request.documentType(), "documentType");
    requireValue(request.documentNumber(), "documentNumber");
    requireValue(request.documentFrontImageDataUrl(), "documentFrontImageDataUrl");
    requireValue(request.documentBackImageDataUrl(), "documentBackImageDataUrl");
    if (request.documentValidUntil() == null) {
      throw new IllegalArgumentException("documentValidUntil is required for manual KYC");
    }
    if (request.documentValidUntil().isBefore(LocalDate.now())) {
      throw new IllegalArgumentException("documentValidUntil must not be in the past");
    }
  }

  private static void requireValue(String value, String fieldName) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(fieldName + " is required for manual KYC");
    }
  }
}
