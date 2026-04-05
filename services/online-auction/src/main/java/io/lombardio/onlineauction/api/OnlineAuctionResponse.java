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
    List<BidderRegistrationResponse> registrations) {

  public OnlineAuctionResponse {
    lots = List.copyOf(lots == null ? List.of() : lots);
    registrations = List.copyOf(registrations == null ? List.of() : registrations);
  }

  @Override
  public List<OnlineAuctionLotResponse> lots() {
    return List.copyOf(lots);
  }

  @Override
  public List<BidderRegistrationResponse> registrations() {
    return List.copyOf(registrations);
  }
}
