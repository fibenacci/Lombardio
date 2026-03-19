package io.lombardio.pawnticket.api.http;

import java.math.BigDecimal;

public record SettlementResponse(
        BigDecimal outstandingLoanAmount,
        BigDecimal interestAmount,
        BigDecimal operatingFeeAmount,
        BigDecimal totalDueAmount,
        String legalText
) {
}
