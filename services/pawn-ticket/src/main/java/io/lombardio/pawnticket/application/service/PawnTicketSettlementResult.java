package io.lombardio.pawnticket.application.service;

import java.math.BigDecimal;

public record PawnTicketSettlementResult(
        BigDecimal outstandingLoanAmount,
        BigDecimal interestAmount,
        BigDecimal operatingFeeAmount,
        BigDecimal totalDueAmount,
        String legalText
) {
}
