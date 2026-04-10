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
package io.lombardio.auction.api.http;

import io.lombardio.auction.domain.model.AuctionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Schema(requiredProperties = {
    "id", "title", "location", "status", "publicAnnouncementDate",
    "auctionDate", "liveStartedAt", "closedAt", "announcementReference", "lots"
})
public record AuctionResponse(
    @NotNull String id,
    @NotNull String title,
    @NotNull String location,
    @NotNull AuctionStatus status,
    @NotNull LocalDate publicAnnouncementDate,
    @NotNull LocalDate auctionDate,
    @NotNull Instant liveStartedAt,
    @NotNull Instant closedAt,
    @NotNull String announcementReference,
    @NotNull List<AuctionLotResponse> lots) {

  public AuctionResponse {
    lots = List.copyOf(lots == null ? List.of() : lots);
  }

  @Override
  public List<AuctionLotResponse> lots() {
    return List.copyOf(lots);
  }
}
