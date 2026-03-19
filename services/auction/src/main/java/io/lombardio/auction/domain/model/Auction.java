package io.lombardio.auction.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record Auction(
        String id,
        String tenantId,
        String title,
        String location,
        AuctionStatus status,
        LocalDate publicAnnouncementDate,
        LocalDate auctionDate,
        Instant liveStartedAt,
        Instant closedAt,
        String announcementReference,
        List<AuctionLot> lots,
        Instant createdAt,
        Instant updatedAt
) {
}
