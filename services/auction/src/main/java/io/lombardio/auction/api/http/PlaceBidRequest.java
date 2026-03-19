package io.lombardio.auction.api.http;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PlaceBidRequest(
        @NotBlank String bidderDisplayName,
        @NotNull @DecimalMin("0.01") BigDecimal amount
) {
}
