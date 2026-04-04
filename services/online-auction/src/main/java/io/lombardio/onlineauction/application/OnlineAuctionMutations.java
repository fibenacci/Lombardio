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
package io.lombardio.onlineauction.application;

import io.lombardio.onlineauction.api.BidderReviewRequest;
import io.lombardio.onlineauction.api.CreateOnlineAuctionRequest;
import io.lombardio.onlineauction.api.PlaceOnlineBidRequest;
import io.lombardio.onlineauction.api.RegisterBidderRequest;
import io.lombardio.onlineauction.domain.BidderApprovalStatus;
import io.lombardio.onlineauction.domain.BidderRegistration;
import io.lombardio.onlineauction.domain.OnlineAuction;
import io.lombardio.onlineauction.domain.OnlineAuctionLot;
import io.lombardio.onlineauction.domain.OnlineAuctionStatus;
import io.lombardio.onlineauction.domain.ReviewCheckStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

final class OnlineAuctionMutations {

  private OnlineAuctionMutations() {}

  static OnlineAuction createDraftAuction(
      String tenantId, CreateOnlineAuctionRequest request, Instant now) {
    String auctionId = "oa-" + UUID.randomUUID();
    String channel = "online-auction:" + tenantId + ":" + auctionId;
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
        createLots(request),
        List.of());
  }

  static OnlineAuction publish(OnlineAuction current, Instant now) {
    return new OnlineAuction(
        current.id(),
        current.tenantId(),
        current.title(),
        current.slug(),
        OnlineAuctionStatus.PUBLISHED,
        current.channelName(),
        current.minimumIncrement(),
        current.countdownSeconds(),
        now,
        current.liveStartedAt(),
        current.countdownEndsAt(),
        current.closedAt(),
        current.createdAt(),
        now,
        current.lots(),
        current.registrations());
  }

  static OnlineAuction start(OnlineAuction current, Instant now) {
    return new OnlineAuction(
        current.id(),
        current.tenantId(),
        current.title(),
        current.slug(),
        OnlineAuctionStatus.LIVE,
        current.channelName(),
        current.minimumIncrement(),
        current.countdownSeconds(),
        current.publishedAt(),
        now,
        now.plusSeconds(current.countdownSeconds()),
        current.closedAt(),
        current.createdAt(),
        now,
        current.lots(),
        current.registrations());
  }

  static OnlineAuction close(OnlineAuction current, Instant now) {
    return new OnlineAuction(
        current.id(),
        current.tenantId(),
        current.title(),
        current.slug(),
        OnlineAuctionStatus.CLOSED,
        current.channelName(),
        current.minimumIncrement(),
        current.countdownSeconds(),
        current.publishedAt(),
        current.liveStartedAt(),
        current.countdownEndsAt(),
        now,
        current.createdAt(),
        now,
        current.lots(),
        current.registrations());
  }

  static BidderRegistration createPersistedRegistration(
      OnlineAuction auction, RegisterBidderRequest request, String rawAccessToken, Instant now) {
    return new BidderRegistration(
        "obr-" + UUID.randomUUID(),
        request.displayName(),
        request.email(),
        request.legalName(),
        request.birthDate(),
        request.iban(),
        "P" + (1000 + auction.registrations().size() + 1),
        null,
        BidderAccessTokenHasher.sha256(rawAccessToken),
        BidderApprovalStatus.PENDING,
        ReviewCheckStatus.PENDING,
        ReviewCheckStatus.PENDING,
        null,
        null,
        now);
  }

  static BidderRegistration exposeAccessToken(
      BidderRegistration persistedRegistration, String rawAccessToken) {
    return new BidderRegistration(
        persistedRegistration.id(),
        persistedRegistration.displayName(),
        persistedRegistration.email(),
        persistedRegistration.legalName(),
        persistedRegistration.birthDate(),
        persistedRegistration.iban(),
        persistedRegistration.paddleNumber(),
        rawAccessToken,
        persistedRegistration.accessTokenHash(),
        persistedRegistration.approvalStatus(),
        persistedRegistration.kycStatus(),
        persistedRegistration.accountCheckStatus(),
        persistedRegistration.reviewNote(),
        persistedRegistration.approvedAt(),
        persistedRegistration.createdAt());
  }

  static OnlineAuction appendRegistration(
      OnlineAuction current, BidderRegistration registration, Instant now) {
    List<BidderRegistration> registrations = new ArrayList<>(current.registrations());
    registrations.add(registration);
    return new OnlineAuction(
        current.id(),
        current.tenantId(),
        current.title(),
        current.slug(),
        current.status(),
        current.channelName(),
        current.minimumIncrement(),
        current.countdownSeconds(),
        current.publishedAt(),
        current.liveStartedAt(),
        current.countdownEndsAt(),
        current.closedAt(),
        current.createdAt(),
        now,
        current.lots(),
        registrations);
  }

  static OnlineAuction reviewRegistration(
      OnlineAuction current,
      String registrationId,
      BidderApprovalStatus approvalStatus,
      ReviewCheckStatus kycStatus,
      ReviewCheckStatus accountCheckStatus,
      BidderReviewRequest request,
      Instant approvedAt,
      Instant updatedAt) {
    List<BidderRegistration> registrations =
        current.registrations().stream()
            .map(
                item ->
                    item.id().equals(registrationId)
                        ? new BidderRegistration(
                            item.id(),
                            item.displayName(),
                            item.email(),
                            item.legalName(),
                            item.birthDate(),
                            item.iban(),
                            item.paddleNumber(),
                            item.accessToken(),
                            item.accessTokenHash(),
                            approvalStatus,
                            kycStatus,
                            accountCheckStatus,
                            request.reviewNote(),
                            approvedAt,
                            item.createdAt())
                        : item)
            .toList();
    return new OnlineAuction(
        current.id(),
        current.tenantId(),
        current.title(),
        current.slug(),
        current.status(),
        current.channelName(),
        current.minimumIncrement(),
        current.countdownSeconds(),
        current.publishedAt(),
        current.liveStartedAt(),
        current.countdownEndsAt(),
        current.closedAt(),
        current.createdAt(),
        updatedAt,
        current.lots(),
        registrations);
  }

  static OnlineAuction applyBid(
      OnlineAuction current,
      PlaceOnlineBidRequest request,
      BidderRegistration bidder,
      Instant updatedAt) {
    List<OnlineAuctionLot> updatedLots =
        current.lots().stream().map(lot -> updateLotBid(current, lot, request, bidder)).toList();
    return new OnlineAuction(
        current.id(),
        current.tenantId(),
        current.title(),
        current.slug(),
        current.status(),
        current.channelName(),
        current.minimumIncrement(),
        current.countdownSeconds(),
        current.publishedAt(),
        current.liveStartedAt(),
        current.countdownEndsAt(),
        current.closedAt(),
        current.createdAt(),
        updatedAt,
        updatedLots,
        current.registrations());
  }

  private static List<OnlineAuctionLot> createLots(CreateOnlineAuctionRequest request) {
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
    return lots;
  }

  private static OnlineAuctionLot updateLotBid(
      OnlineAuction auction,
      OnlineAuctionLot lot,
      PlaceOnlineBidRequest request,
      BidderRegistration bidder) {
    if (!lot.id().equals(request.lotId())) {
      return lot;
    }
    BigDecimal minimumBid = lot.currentBid().max(lot.startingBid()).add(auction.minimumIncrement());
    if (request.amount().compareTo(minimumBid) < 0) {
      throw new IllegalArgumentException("Bid must satisfy the minimum increment");
    }
    return new OnlineAuctionLot(
        lot.id(),
        lot.lotNumber(),
        lot.title(),
        lot.description(),
        lot.startingBid(),
        request.amount(),
        bidder.paddleNumber());
  }

  private static String normalizeSlug(String value) {
    return value.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
  }
}
