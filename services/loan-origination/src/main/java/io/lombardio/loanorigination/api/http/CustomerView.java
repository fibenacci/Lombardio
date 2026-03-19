package io.lombardio.loanorigination.api.http;

import java.time.LocalDate;

public record CustomerView(
        String id,
        String customerNumber,
        String displayName,
        LocalDate birthDate,
        String phone,
        String checkedDocumentType
) {
}
