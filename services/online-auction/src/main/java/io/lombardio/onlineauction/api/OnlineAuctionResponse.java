package io.lombardio.onlineauction.api;

import io.lombardio.onlineauction.domain.OnlineAuctionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OnlineAuctionResponse(
        String id,
        String tenantId,
        String title,
        String slug,
        OnlineAuctionStatus status,
        String channelName,
        BigDecimal minimumIncrement,
        int countdownSeconds,
        Instant publishedAt,
        Instant liveStartedAt,
        Instant countdownEndsAt,
        Instant closedAt,
        List<OnlineAuctionLotResponse> lots,
        List<BidderRegistrationResponse> registrations
) {
}
