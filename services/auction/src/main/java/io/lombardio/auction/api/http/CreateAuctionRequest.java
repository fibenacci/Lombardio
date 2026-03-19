package io.lombardio.auction.api.http;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateAuctionRequest(
        @NotBlank String title,
        @NotBlank String location,
        @NotEmpty List<@Valid AuctionLotRequest> lots
) {
}
