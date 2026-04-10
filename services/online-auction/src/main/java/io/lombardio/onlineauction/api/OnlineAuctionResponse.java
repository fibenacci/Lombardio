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
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Schema(
    requiredProperties = {
      "id",
      "tenantId",
      "title",
      "slug",
      "status",
      "channelName",
      "minimumIncrement",
      "countdownSeconds",
      "publishedAt",
      "liveStartedAt",
      "countdownEndsAt",
      "closedAt",
      "createdAt",
      "updatedAt",
      "lots",
      "registrations"
    })
public record OnlineAuctionResponse(
    @NotNull String id,
    @NotNull String tenantId,
    @NotNull String title,
    @NotNull String slug,
    @NotNull OnlineAuctionStatus status,
    @NotNull String channelName,
    @NotNull BigDecimal minimumIncrement,
    @NotNull int countdownSeconds,
    @NotNull Instant publishedAt,
    @NotNull Instant liveStartedAt,
    @NotNull Instant countdownEndsAt,
    @NotNull Instant closedAt,
    @NotNull Instant createdAt,
    @NotNull Instant updatedAt,
    @NotNull List<OnlineAuctionLotResponse> lots,
    @NotNull List<BidderRegistrationResponse> registrations) {

  public OnlineAuctionResponse {
    lots = List.copyOf(lots == null ? List.of() : lots);
    registrations = List.copyOf(registrations == null ? List.of() : registrations);
  }

  public OnlineAuctionResponse withRegistrations(List<BidderRegistrationResponse> registrations) {
    return new OnlineAuctionResponse(
        id,
        tenantId,
        title,
        slug,
        status,
        channelName,
        minimumIncrement,
        countdownSeconds,
        publishedAt,
        liveStartedAt,
        countdownEndsAt,
        closedAt,
        createdAt,
        updatedAt,
        lots,
        registrations);
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
