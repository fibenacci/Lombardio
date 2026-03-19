package io.lombardio.pawnticket.api.http;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RedeemPawnTicketRequest(
        @NotNull @DecimalMin("0.01") BigDecimal outstandingLoanAmount,
        Integer remainingTermMonths,
        @DecimalMin("0.00") BigDecimal manualMonthlyOperatingFee
) {
}
