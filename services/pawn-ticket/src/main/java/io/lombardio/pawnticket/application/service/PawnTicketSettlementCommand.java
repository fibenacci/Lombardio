package io.lombardio.pawnticket.application.service;

import java.math.BigDecimal;

public record PawnTicketSettlementCommand(
        BigDecimal outstandingLoanAmount,
        BigDecimal repaymentAmount,
        Integer remainingTermMonths,
        Integer extensionMonths,
        BigDecimal manualMonthlyOperatingFee
) {
}
