package io.lombardio.onlineauction.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

import java.math.BigDecimal;
import java.util.List;

public record CreateOnlineAuctionRequest(
        @NotBlank String title,
        @NotBlank String slug,
        @NotNull @DecimalMin("1.00") BigDecimal minimumIncrement,
        @Min(30) int countdownSeconds,
        @NotEmpty List<@Valid OnlineAuctionLotRequest> lots
) {
}
