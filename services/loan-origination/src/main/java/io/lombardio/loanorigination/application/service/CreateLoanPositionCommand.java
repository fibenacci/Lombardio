package io.lombardio.loanorigination.application.service;

import java.math.BigDecimal;

public record CreateLoanPositionCommand(
        Integer ticketGroup,
        String label,
        String description,
        String guidelineId,
        BigDecimal pledgedValue
) {
}
