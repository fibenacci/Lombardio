package io.lombardio.pawnticket.api.http;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PawnTicketPositionPayload(
        @NotBlank String label,
        @NotBlank String description,
        @NotNull @DecimalMin("0.01") BigDecimal pledgedValue
) {
}
