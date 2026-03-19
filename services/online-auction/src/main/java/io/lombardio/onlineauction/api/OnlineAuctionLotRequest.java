package io.lombardio.onlineauction.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OnlineAuctionLotRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotNull @DecimalMin("0.01") BigDecimal startingBid
) {
}
