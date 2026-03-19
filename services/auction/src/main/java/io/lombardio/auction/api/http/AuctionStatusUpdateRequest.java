package io.lombardio.auction.api.http;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record AuctionStatusUpdateRequest(
        LocalDate auctionDate,
        @NotBlank String announcementReference
) {
}
