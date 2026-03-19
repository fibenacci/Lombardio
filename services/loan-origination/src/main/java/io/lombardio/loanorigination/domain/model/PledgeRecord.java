package io.lombardio.loanorigination.domain.model;

import java.time.Instant;
import java.time.LocalDate;

public record PledgeRecord(
        String id,
        String loanCaseId,
        String tenantId,
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
        String bearerCity,
        String powerOfAttorneyDocumentDataUrl
) {
}
