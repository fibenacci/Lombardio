package io.lombardio.pawnticket.api.http;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;

import java.math.BigDecimal;
import java.util.List;

public record IssuePawnTicketRequest(
        @NotBlank String tenantId,
        @NotBlank String customerId,
        @NotBlank String customerNumber,
        @NotBlank String customerDisplayName,
        String customerPhone,
        @NotNull @DecimalMin("0.01") BigDecimal loanAmount,
        @Min(3) Integer termMonths,
        @DecimalMin("0.00") BigDecimal manualMonthlyOperatingFee,
        @NotEmpty List<@Valid PawnTicketPositionPayload> positions
) {
}
