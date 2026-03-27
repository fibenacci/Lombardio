package io.lombardio.identity.kyc.api;

import io.lombardio.identity.kyc.domain.KycStatus;
import io.lombardio.identity.kyc.domain.KycVerificationMode;

import java.time.LocalDate;

public record KycStatusResponse(
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
        String providerStatus,
        boolean providerVerificationAvailable
) {
}
