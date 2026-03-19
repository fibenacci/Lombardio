package io.lombardio.customer.api.http;

import java.time.LocalDate;

public record CustomerResponse(
        String id,
        String customerNumber,
        String firstName,
        String lastName,
        LocalDate birthDate,
        String displayName,
        String phone,
        String kycStatus,
        boolean kycApproved,
        String checkedDocumentType,
        String street,
        String postalCode,
        String city
) {
}
