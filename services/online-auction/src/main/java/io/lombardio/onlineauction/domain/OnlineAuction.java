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

import io.lombardio.onlineauction.api.CreateOnlineAuctionRequest;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public record OnlineAuction(
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
    Instant createdAt,
    Instant updatedAt,
    List<OnlineAuctionLot> lots,
    List<BidderRegistration> registrations) {

  public OnlineAuction {
    lots = List.copyOf(lots != null ? lots : List.of());
    registrations = List.copyOf(registrations != null ? registrations : List.of());
  }

  public static OnlineAuction createDraft(
      String tenantId, CreateOnlineAuctionRequest request, Instant now) {
    String auctionId = "oa-" + UUID.randomUUID();
    String channel = "online-auction:" + tenantId + ":" + auctionId;
    List<OnlineAuctionLot> lots = new ArrayList<>();
    for (int index = 0; index < request.lots().size(); index++) {
      var lot = request.lots().get(index);
      lots.add(
          new OnlineAuctionLot(
              "oal-" + UUID.randomUUID(),
              index + 1,
              lot.title(),
              lot.description(),
              lot.startingBid(),
              lot.startingBid(),
              null));
    }

    return new OnlineAuction(
        auctionId,
        tenantId,
        request.title(),
        normalizeSlug(request.slug()),
        OnlineAuctionStatus.DRAFT,
        channel,
        request.minimumIncrement(),
        request.countdownSeconds(),
        null,
        null,
        null,
        null,
        now,
        now,
        lots,
        List.of());
  }

  public OnlineAuction publish(Instant now) {
    if (status != OnlineAuctionStatus.DRAFT) {
      throw new IllegalArgumentException("Only draft auctions can be published");
    }
    return new OnlineAuction(
        id,
        tenantId,
        title,
        slug,
        OnlineAuctionStatus.PUBLISHED,
        channelName,
        minimumIncrement,
        countdownSeconds,
        now,
        liveStartedAt,
        countdownEndsAt,
        closedAt,
        createdAt,
        now,
        lots,
        registrations);
  }

  public OnlineAuction start(Instant now) {
    if (status != OnlineAuctionStatus.PUBLISHED) {
      throw new IllegalArgumentException("Only published auctions can go live");
    }
    return new OnlineAuction(
        id,
        tenantId,
        title,
        slug,
        OnlineAuctionStatus.LIVE,
        channelName,
        minimumIncrement,
        countdownSeconds,
        publishedAt,
        now,
        now.plusSeconds(countdownSeconds),
        closedAt,
        createdAt,
        now,
        lots,
        registrations);
  }

  public OnlineAuction close(Instant now) {
    if (status != OnlineAuctionStatus.LIVE) {
      throw new IllegalArgumentException("Only live auctions can be closed");
    }
    return new OnlineAuction(
        id,
        tenantId,
        title,
        slug,
        OnlineAuctionStatus.CLOSED,
        channelName,
        minimumIncrement,
        countdownSeconds,
        publishedAt,
        liveStartedAt,
        countdownEndsAt,
        now,
        createdAt,
        now,
        lots,
        registrations);
  }

  public OnlineAuction applyBid(
      String lotId, BigDecimal amount, BidderRegistration bidder, Instant now) {
    List<OnlineAuctionLot> updatedLots =
        lots.stream()
            .map(lot -> lot.id().equals(lotId) ? lot.applyBid(this, bidder, amount) : lot)
            .toList();
    return new OnlineAuction(
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
        now,
        updatedLots,
        registrations);
  }

  public OnlineAuction appendRegistration(BidderRegistration registration, Instant now) {
    List<BidderRegistration> updatedRegistrations = new ArrayList<>(registrations);
    updatedRegistrations.add(registration);
    return new OnlineAuction(
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
        now,
        lots,
        updatedRegistrations);
  }

  public OnlineAuction reviewRegistration(
      String registrationId, BidderRegistration updatedRegistration, Instant now) {
    List<BidderRegistration> updatedRegistrations =
        registrations.stream()
            .map(item -> item.id().equals(registrationId) ? updatedRegistration : item)
            .toList();
    return new OnlineAuction(
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
        now,
        lots,
        updatedRegistrations);
  }

  private static String normalizeSlug(String value) {
    return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
  }
}
