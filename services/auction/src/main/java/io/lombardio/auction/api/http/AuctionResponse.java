package io.lombardio.auction.api.http;

import io.lombardio.auction.domain.model.AuctionStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record AuctionResponse(
        String id,
        String title,
        String location,
        AuctionStatus status,
        LocalDate publicAnnouncementDate,
        LocalDate auctionDate,
        Instant liveStartedAt,
        Instant closedAt,
        String announcementReference,
        List<AuctionLotResponse> lots
) {
}
