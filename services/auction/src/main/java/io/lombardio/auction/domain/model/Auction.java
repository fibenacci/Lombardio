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
package io.lombardio.auction.domain.model;

import java.math.BigDecimal;
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
    Instant updatedAt) {

  public Auction {
    lots = List.copyOf(lots != null ? lots : List.of());
  }

  public Auction announce(
      LocalDate announcementDate, LocalDate auctionDate, String reference, Instant now) {
    if (auctionDate == null) {
      throw new IllegalArgumentException("auctionDate is required");
    }
    if (auctionDate.isBefore(announcementDate.plusDays(7))
        || auctionDate.isAfter(announcementDate.plusDays(14))) {
      throw new IllegalArgumentException(
          "auctionDate must be between 7 and 14 days after public announcement");
    }

    return new Auction(
        id,
        tenantId,
        title,
        location,
        AuctionStatus.ANNOUNCED,
        announcementDate,
        auctionDate,
        liveStartedAt,
        closedAt,
        reference,
        lots,
        createdAt,
        now);
  }

  public Auction open(Instant now) {
    if (status != AuctionStatus.ANNOUNCED) {
      throw new IllegalArgumentException("Only announced auctions can be opened");
    }
    List<AuctionLot> openedLots = lots.stream().map(AuctionLot::open).toList();
    return new Auction(
        id,
        tenantId,
        title,
        location,
        AuctionStatus.LIVE,
        publicAnnouncementDate,
        auctionDate,
        now,
        closedAt,
        announcementReference,
        openedLots,
        createdAt,
        now);
  }

  public Auction close(Instant now) {
    return new Auction(
        id,
        tenantId,
        title,
        location,
        AuctionStatus.CLOSED,
        publicAnnouncementDate,
        auctionDate,
        liveStartedAt,
        now,
        announcementReference,
        lots,
        createdAt,
        now);
  }

  public Auction placeBid(String lotId, String bidder, BigDecimal amount, Instant now) {
    if (status != AuctionStatus.LIVE) {
      throw new IllegalArgumentException("Auction must be live before bids can be placed");
    }
    List<AuctionLot> updatedLots =
        lots.stream()
            .map(lot -> lot.id().equals(lotId) ? lot.placeBid(bidder, amount) : lot)
            .toList();
    return new Auction(
        id,
        tenantId,
        title,
        location,
        status,
        publicAnnouncementDate,
        auctionDate,
        liveStartedAt,
        closedAt,
        announcementReference,
        updatedLots,
        createdAt,
        now);
  }

  public Auction settleLot(
      String lotId, BigDecimal hammerPrice, LocalDate settlementDate, Instant now) {
    List<AuctionLot> updatedLots =
        lots.stream()
            .map(lot -> lot.id().equals(lotId) ? lot.settle(hammerPrice, settlementDate) : lot)
            .toList();
    return new Auction(
        id,
        tenantId,
        title,
        location,
        status,
        publicAnnouncementDate,
        auctionDate,
        liveStartedAt,
        closedAt,
        announcementReference,
        updatedLots,
        createdAt,
        now);
  }
}
