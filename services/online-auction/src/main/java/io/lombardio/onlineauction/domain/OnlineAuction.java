/*
 * Lombardio Source-Available No-Distribution License 1.0
 *
 * Copyright (c) 2026 Benjamin Letzel. All rights reserved.
 *
 * This project is source-available for educational and review purposes only.
 * Redistribution, sublicensing, or commercial use is strictly prohibited.
 *
 * For partnership or cooperation inquiries, please contact the author.
 */
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
    List<BidderRegistration> registrations) {}
