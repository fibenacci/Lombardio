package io.lombardio.kyc.api;

import java.time.LocalDate;

public record DocumentPrefillResponse(
        boolean available,
        boolean matched,
        String documentType,
        String documentNumber,
        LocalDate documentValidUntil,
        String providerName,
        double confidence
) {
}
