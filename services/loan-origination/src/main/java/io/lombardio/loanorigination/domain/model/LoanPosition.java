package io.lombardio.loanorigination.domain.model;

import java.math.BigDecimal;

public record LoanPosition(
        String id,
        Integer ticketGroup,
        String label,
        String description,
        String guidelineId,
        String guidelineLabel,
        BigDecimal baseLoanValue,
        BigDecimal pledgedValue
) {
}
