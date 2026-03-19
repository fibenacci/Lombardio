package io.lombardio.pawnticket.api.http;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ExtendPawnTicketRequest(
        @NotNull @DecimalMin("0.01") BigDecimal outstandingLoanAmount,
        @Min(1) Integer extensionMonths,
        @DecimalMin("0.00") BigDecimal manualMonthlyOperatingFee
) {
}
