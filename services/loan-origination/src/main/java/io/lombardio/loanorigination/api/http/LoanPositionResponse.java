package io.lombardio.loanorigination.api.http;

import java.math.BigDecimal;

public record LoanPositionResponse(
        String id,
        Integer ticketGroup,
        String label,
        String description,
        String guidelineLabel,
        BigDecimal baseLoanValue,
        BigDecimal pledgedValue
) {
}
