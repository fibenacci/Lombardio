package io.lombardio.loanorigination.api.http;

import java.time.Instant;
import java.time.LocalDate;

public record PledgeRecordResponse(
        String id,
        Instant recordedAt,
        String languageCode,
        LocalDate retentionUntil,
        String pledgorName,
        String pledgorStreet,
        String pledgorPostalCode,
        String pledgorCity,
        LocalDate pledgorBirthDate,
        String checkedDocumentType,
        boolean powerOfAttorneyRequired,
        String bearerName,
        String bearerStreet,
        String bearerPostalCode,
        String bearerCity
) {
}
