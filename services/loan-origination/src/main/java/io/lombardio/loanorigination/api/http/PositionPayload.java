package io.lombardio.loanorigination.api.http;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PositionPayload(
        @NotNull @Min(1) Integer ticketGroup,
        @NotBlank String label,
        @NotBlank String description,
        @NotBlank String guidelineId,
        @DecimalMin("0.01") BigDecimal pledgedValue
) {
}
