package io.lombardio.auction.application.service;

import java.time.LocalDate;

public record AnnounceAuctionCommand(
        LocalDate auctionDate,
        String announcementReference
) {
}
