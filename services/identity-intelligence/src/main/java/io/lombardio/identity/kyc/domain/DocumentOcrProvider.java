package io.lombardio.identity.kyc.domain;

import java.time.LocalDate;
import java.util.Optional;

public interface DocumentOcrProvider {

    Optional<DocumentOcrResult> prefill(String tenantId, String frontImageDataUrl, String backImageDataUrl);

    record DocumentOcrResult(
            String firstName,
            String lastName,
            LocalDate birthDate,
            String documentType,
            String documentNumber,
            LocalDate documentValidUntil,
            String portraitImageDataUrl,
            String providerName,
            double confidence
    ) {
    }
}
