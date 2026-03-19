package io.lombardio.auction.api.http;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AuctionSettlementRequest(
        @NotNull @DecimalMin("0.01") BigDecimal hammerPrice
) {
}
