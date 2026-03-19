package io.lombardio.pawnticket.api.http;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PawnTicketQuoteRequest(
        @NotNull @DecimalMin("0.01") BigDecimal loanAmount,
        @Min(3) Integer termMonths,
        @DecimalMin("0.00") BigDecimal manualMonthlyOperatingFee
) {
}
