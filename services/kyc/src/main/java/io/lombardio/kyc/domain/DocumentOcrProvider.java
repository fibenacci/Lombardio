package io.lombardio.kyc.domain;

import java.time.LocalDate;
import java.util.Optional;

public interface DocumentOcrProvider {

    Optional<DocumentOcrResult> prefill(String tenantId, String frontImageDataUrl, String backImageDataUrl);

    record DocumentOcrResult(
            String documentType,
            String documentNumber,
            LocalDate documentValidUntil,
            String providerName,
            double confidence
    ) {
    }
}
