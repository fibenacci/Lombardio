package io.lombardio.onlineauction.domain;

import java.time.Instant;
import java.util.List;

public record OnlineAuction(
        String id,
        String tenantId,
        String title,
        String slug,
        OnlineAuctionStatus status,
        String channelName,
        java.math.BigDecimal minimumIncrement,
        int countdownSeconds,
        Instant publishedAt,
        Instant liveStartedAt,
        Instant countdownEndsAt,
        Instant closedAt,
        Instant createdAt,
        Instant updatedAt,
        List<OnlineAuctionLot> lots,
        List<BidderRegistration> registrations
) {
}
