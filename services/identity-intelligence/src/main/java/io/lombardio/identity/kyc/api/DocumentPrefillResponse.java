package io.lombardio.identity.kyc.api;

import java.time.LocalDate;

public record DocumentPrefillResponse(
        boolean available,
        boolean matched,
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
