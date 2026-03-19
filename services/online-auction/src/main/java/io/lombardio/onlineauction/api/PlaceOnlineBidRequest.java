package io.lombardio.onlineauction.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PlaceOnlineBidRequest(
        @NotBlank String accessToken,
        @NotBlank String lotId,
        @NotNull @DecimalMin("0.01") BigDecimal amount
) {
}
