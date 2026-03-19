package io.lombardio.auction.api.http;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AuctionLotRequest(
        @NotBlank String contractNumber,
        @NotBlank String itemNumber,
        @NotBlank String description,
        @NotNull @DecimalMin("0.01") BigDecimal estimatedValue,
        @NotNull @DecimalMin("0.01") BigDecimal outstandingClaim
) {
}
