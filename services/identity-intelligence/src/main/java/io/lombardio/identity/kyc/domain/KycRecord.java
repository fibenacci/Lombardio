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
package io.lombardio.identity.kyc.domain;

import java.time.LocalDate;

public record KycRecord(
    String id,
    String tenantId,
    String customerId,
    KycVerificationMode verificationMode,
    KycStatus status,
    LocalDate verifiedUntil,
    String documentType,
    String documentNumber,
    LocalDate documentValidUntil,
    String documentFrontImageDataUrl,
    String documentBackImageDataUrl,
    String decisionNote,
    String providerName,
    String providerReference,
    String providerStatus) {}
