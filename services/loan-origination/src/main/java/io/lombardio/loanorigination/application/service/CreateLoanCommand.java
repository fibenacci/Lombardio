package io.lombardio.loanorigination.application.service;

import java.math.BigDecimal;
import java.util.List;

public record CreateLoanCommand(
        String customerId,
        List<CreateLoanPositionCommand> positions,
        Integer termMonths,
        BigDecimal manualMonthlyOperatingFee,
        boolean thirdPartyPledgorPresentation,
        String bearerName,
        String bearerStreet,
        String bearerPostalCode,
        String bearerCity,
        String powerOfAttorneyDocumentDataUrl
) {
}
