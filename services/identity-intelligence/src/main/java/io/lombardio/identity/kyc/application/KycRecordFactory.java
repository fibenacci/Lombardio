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

import io.lombardio.identity.kyc.domain.KycRecord;
import io.lombardio.identity.kyc.domain.KycStatus;
import io.lombardio.identity.kyc.domain.KycVerificationMode;
import java.util.UUID;

final class KycRecordFactory {

  private KycRecordFactory() {}

  static KycRecord empty(String tenantId, String customerId) {
    return new KycRecord(
        "kyc-" + UUID.randomUUID(),
        tenantId,
        customerId,
        KycVerificationMode.MANUAL,
        KycStatus.NOT_STARTED,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null);
  }

  static KycRecord update(
      KycRecord existing,
      String tenantId,
      String customerId,
      KycVerificationMode verificationMode,
      UpdateKycStatusCommand request) {
    return new KycRecord(
        existing.id(),
        tenantId,
        customerId,
        verificationMode,
        request.status(),
        request.verifiedUntil(),
        request.documentType(),
        request.documentNumber(),
        request.documentValidUntil(),
        DocumentImageDataUrlNormalizer.normalize(request.documentFrontImageDataUrl()),
        DocumentImageDataUrlNormalizer.normalize(request.documentBackImageDataUrl()),
        request.decisionNote(),
        verificationMode == KycVerificationMode.PROVIDER ? request.providerName() : null,
        verificationMode == KycVerificationMode.PROVIDER ? request.providerReference() : null,
        verificationMode == KycVerificationMode.PROVIDER ? request.providerStatus() : null);
  }
}
