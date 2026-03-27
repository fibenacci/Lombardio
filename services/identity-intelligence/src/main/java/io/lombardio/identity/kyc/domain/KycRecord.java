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
        String providerStatus
) {
}
