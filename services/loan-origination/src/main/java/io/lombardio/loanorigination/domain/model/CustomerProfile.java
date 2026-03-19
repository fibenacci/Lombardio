package io.lombardio.loanorigination.domain.model;

import java.time.LocalDate;

public record CustomerProfile(
        String id,
        String tenantId,
        String customerNumber,
        String displayName,
        LocalDate birthDate,
        String phone,
        String street,
        String postalCode,
        String city,
        String kycStatus,
        boolean kycApproved,
        String checkedDocumentType
) {
}
