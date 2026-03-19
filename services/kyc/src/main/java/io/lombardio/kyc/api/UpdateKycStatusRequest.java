package io.lombardio.kyc.api;

import io.lombardio.kyc.domain.KycStatus;
import io.lombardio.kyc.domain.KycVerificationMode;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateKycStatusRequest(
        @NotNull KycStatus status,
        KycVerificationMode verificationMode,
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
