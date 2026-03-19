package io.lombardio.pawnticket.api.http;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PartialRepaymentRequest(
        @NotNull @DecimalMin("0.01") BigDecimal outstandingLoanAmount,
        @NotNull @DecimalMin("0.01") BigDecimal repaymentAmount,
        Integer remainingTermMonths,
        @DecimalMin("0.00") BigDecimal manualMonthlyOperatingFee
) {
}
