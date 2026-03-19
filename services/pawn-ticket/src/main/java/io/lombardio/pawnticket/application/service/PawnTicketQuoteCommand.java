package io.lombardio.pawnticket.application.service;

import java.math.BigDecimal;

public record PawnTicketQuoteCommand(
        BigDecimal loanAmount,
        Integer termMonths,
        BigDecimal manualMonthlyOperatingFee
) {
}
