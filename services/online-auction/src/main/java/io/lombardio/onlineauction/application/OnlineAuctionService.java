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

import io.lombardio.onlineauction.api.BidderRegistrationResponse;
import io.lombardio.onlineauction.api.BidderReviewRequest;
import io.lombardio.onlineauction.api.CreateOnlineAuctionRequest;
import io.lombardio.onlineauction.api.OnlineAuctionLotResponse;
import io.lombardio.onlineauction.api.OnlineAuctionNotFoundException;
import io.lombardio.onlineauction.api.OnlineAuctionResponse;
import io.lombardio.onlineauction.api.PlaceOnlineBidRequest;
import io.lombardio.onlineauction.api.RealtimeSessionResponse;
import io.lombardio.onlineauction.api.RegisterBidderRequest;
import io.lombardio.onlineauction.domain.BidderApprovalStatus;
import io.lombardio.onlineauction.domain.BidderRegistration;
import io.lombardio.onlineauction.domain.OnlineAuction;
import io.lombardio.onlineauction.domain.OnlineAuctionLot;
import io.lombardio.onlineauction.domain.OnlineAuctionRepository;
import io.lombardio.onlineauction.domain.OnlineAuctionStatus;
import io.lombardio.onlineauction.domain.RealtimePublisher;
import io.lombardio.onlineauction.domain.RealtimeSession;
import io.lombardio.onlineauction.domain.RealtimeSessionTokenService;
import io.lombardio.onlineauction.domain.ReviewCheckStatus;
import io.micrometer.core.instrument.MeterRegistry;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OnlineAuctionService {

  private final OnlineAuctionRepository auctionRepository;
  private final RealtimePublisher realtimePublisher;
  private final RealtimeSessionTokenService realtimeSessionTokenService;
  private final OnlineAuctionMetrics metrics;

  public OnlineAuctionService(
      OnlineAuctionRepository auctionRepository,
      RealtimePublisher realtimePublisher,
      RealtimeSessionTokenService realtimeSessionTokenService) {
    this(
        auctionRepository,
        realtimePublisher,
        realtimeSessionTokenService,
        OnlineAuctionMetrics.noop());
  }

  @Autowired
  public OnlineAuctionService(
      OnlineAuctionRepository auctionRepository,
      RealtimePublisher realtimePublisher,
      RealtimeSessionTokenService realtimeSessionTokenService,
      MeterRegistry meterRegistry) {
    this(
        auctionRepository,
        realtimePublisher,
        realtimeSessionTokenService,
        new OnlineAuctionMetrics(meterRegistry));
  }

  private OnlineAuctionService(
      OnlineAuctionRepository auctionRepository,
      RealtimePublisher realtimePublisher,
      RealtimeSessionTokenService realtimeSessionTokenService,
      OnlineAuctionMetrics metrics) {
    this.auctionRepository = auctionRepository;
    this.realtimePublisher = realtimePublisher;
    this.realtimeSessionTokenService = realtimeSessionTokenService;
    this.metrics = metrics;
  }

  public List<OnlineAuctionResponse> listAdminAuctions(String tenantId) {
    return auctionRepository.findByTenantId(tenantId).stream()
        .sorted(Comparator.comparing(OnlineAuction::createdAt).reversed())
        .map(this::toResponse)
        .toList();
  }

  public OnlineAuctionResponse createAuction(String tenantId, CreateOnlineAuctionRequest request) {
    Instant now = Instant.now();
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
    OnlineAuction auction =
        new OnlineAuction(
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
    OnlineAuction saved = auctionRepository.save(auction);
    metrics.recordAuctionCreated();
    return toResponse(saved);
  }

  public OnlineAuctionResponse publishAuction(String tenantId, String auctionId) {
    OnlineAuction current = requireAuction(tenantId, auctionId);
    if (current.status() != OnlineAuctionStatus.DRAFT) {
      throw new IllegalArgumentException("Only draft auctions can be published");
    }
    OnlineAuction updated =
        new OnlineAuction(
            current.id(),
            current.tenantId(),
            current.title(),
            current.slug(),
            OnlineAuctionStatus.PUBLISHED,
            current.channelName(),
            current.minimumIncrement(),
            current.countdownSeconds(),
            Instant.now(),
            current.liveStartedAt(),
            current.countdownEndsAt(),
            current.closedAt(),
            current.createdAt(),
            Instant.now(),
            current.lots(),
            current.registrations());
    OnlineAuction saved = auctionRepository.save(updated);
    publish(saved.channelName(), "auction_published", saved);
    return toResponse(saved);
  }

  public OnlineAuctionResponse startAuction(String tenantId, String auctionId) {
    OnlineAuction current = requireAuction(tenantId, auctionId);
    if (current.status() != OnlineAuctionStatus.PUBLISHED) {
      throw new IllegalArgumentException("Only published auctions can go live");
    }
    Instant now = Instant.now();
    OnlineAuction updated =
        new OnlineAuction(
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
            Instant.now(),
            current.lots(),
            current.registrations());
    OnlineAuction saved = auctionRepository.save(updated);
    publish(saved.channelName(), "auction_live", saved);
    return toResponse(saved);
  }

  public OnlineAuctionResponse closeAuction(String tenantId, String auctionId) {
    OnlineAuction current = requireAuction(tenantId, auctionId);
    if (current.status() != OnlineAuctionStatus.LIVE) {
      throw new IllegalArgumentException("Only live auctions can be closed");
    }
    OnlineAuction updated =
        new OnlineAuction(
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
            Instant.now(),
            current.createdAt(),
            Instant.now(),
            current.lots(),
            current.registrations());
    OnlineAuction saved = auctionRepository.save(updated);
    publish(saved.channelName(), "auction_closed", saved);
    return toResponse(saved);
  }

  public List<OnlineAuctionResponse> listPublicAuctions(String tenantId) {
    return auctionRepository.findPublicByTenantId(tenantId).stream().map(this::toResponse).toList();
  }

  public OnlineAuctionResponse getPublicAuction(String tenantId, String auctionId) {
    return toResponse(requirePublicAuction(tenantId, auctionId));
  }

  public BidderRegistrationResponse registerBidder(
      String tenantId, String auctionId, RegisterBidderRequest request) {
    OnlineAuction current = requirePublicAuction(tenantId, auctionId);
    BidderRegistration registration =
        new BidderRegistration(
            "obr-" + UUID.randomUUID(),
            request.displayName(),
            request.email(),
            request.legalName(),
            request.birthDate(),
            request.iban(),
            "P" + (1000 + current.registrations().size() + 1),
            UUID.randomUUID().toString().replace("-", ""),
            BidderApprovalStatus.PENDING,
            ReviewCheckStatus.PENDING,
            ReviewCheckStatus.PENDING,
            null,
            null,
            Instant.now());
    List<BidderRegistration> registrations = new ArrayList<>(current.registrations());
    registrations.add(registration);
    OnlineAuction updated =
        new OnlineAuction(
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
            Instant.now(),
            current.lots(),
            registrations);
    OnlineAuction saved = auctionRepository.save(updated);
    publish(saved.channelName(), "bidder_registered", registration);
    metrics.recordBidderRegistration();
    return toRegistrationResponse(registration);
  }

  public OnlineAuctionResponse reviewRegistration(
      String tenantId, String auctionId, String registrationId, BidderReviewRequest request) {
    OnlineAuction current = requireAuction(tenantId, auctionId);
    ReviewCheckStatus kycStatus = parseCheckStatus(request.kycStatus());
    ReviewCheckStatus accountCheckStatus = parseCheckStatus(request.accountCheckStatus());
    BidderApprovalStatus status =
        switch (request.decision().trim().toUpperCase()) {
          case "APPROVE", "APPROVED" -> BidderApprovalStatus.APPROVED;
          case "REJECT", "REJECTED" -> BidderApprovalStatus.REJECTED;
          default -> throw new IllegalArgumentException("Decision must be APPROVE or REJECT");
        };
    if (status == BidderApprovalStatus.APPROVED
        && (kycStatus != ReviewCheckStatus.PASSED
            || accountCheckStatus != ReviewCheckStatus.PASSED)) {
      throw new IllegalArgumentException("Bidder approval requires passed KYC and account checks");
    }
    Instant approvedAt = status == BidderApprovalStatus.APPROVED ? Instant.now() : null;
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
                            status,
                            kycStatus,
                            accountCheckStatus,
                            request.reviewNote(),
                            approvedAt,
                            item.createdAt())
                        : item)
            .toList();
    OnlineAuction updated =
        new OnlineAuction(
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
            Instant.now(),
            current.lots(),
            registrations);
    OnlineAuction saved = auctionRepository.save(updated);
    publish(saved.channelName(), "registration_reviewed", toResponse(saved));
    metrics.recordBidderReview(status, kycStatus, accountCheckStatus);
    return toResponse(saved);
  }

  public OnlineAuctionResponse placeBid(
      String tenantId, String auctionId, PlaceOnlineBidRequest request) {
    OnlineAuction current = requirePublicAuction(tenantId, auctionId);
    if (current.status() != OnlineAuctionStatus.LIVE) {
      throw new IllegalArgumentException("Auction is not live");
    }
    if (current.countdownEndsAt() != null && Instant.now().isAfter(current.countdownEndsAt())) {
      throw new IllegalArgumentException("Auction countdown has ended");
    }
    BidderRegistration bidder =
        current.registrations().stream()
            .filter(item -> item.accessToken().equals(request.accessToken()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown bidder session"));
    if (bidder.approvalStatus() != BidderApprovalStatus.APPROVED) {
      throw new IllegalArgumentException("Bidder is not approved for live bidding");
    }
    List<OnlineAuctionLot> updatedLots =
        current.lots().stream().map(lot -> updateLotBid(current, lot, request, bidder)).toList();
    OnlineAuction updated =
        new OnlineAuction(
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
            Instant.now(),
            updatedLots,
            current.registrations());
    OnlineAuction saved = auctionRepository.save(updated);
    publish(saved.channelName(), "bid_placed", toResponse(saved));
    metrics.recordBidPlaced(request.amount());
    return toResponse(saved);
  }

  public RealtimeSessionResponse issueRealtimeSession(
      String tenantId, String auctionId, String accessToken) {
    OnlineAuction auction = requirePublicAuction(tenantId, auctionId);
    BidderRegistration bidder =
        auction.registrations().stream()
            .filter(item -> item.accessToken().equals(accessToken))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown bidder session"));
    if (bidder.approvalStatus() != BidderApprovalStatus.APPROVED) {
      throw new IllegalArgumentException("Bidder is not approved for realtime access");
    }
    if (bidder.kycStatus() != ReviewCheckStatus.PASSED
        || bidder.accountCheckStatus() != ReviewCheckStatus.PASSED) {
      throw new IllegalArgumentException("Bidder compliance checks are incomplete");
    }
    RealtimeSession session =
        realtimeSessionTokenService.createSession(bidder.paddleNumber(), auction.channelName());
    return new RealtimeSessionResponse(
        session.wsUrl(), session.channel(), session.connectionToken(), session.subscriptionToken());
  }

  private OnlineAuctionLot updateLotBid(
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

  private OnlineAuction requireAuction(String tenantId, String auctionId) {
    return auctionRepository
        .findByTenantIdAndId(tenantId, auctionId)
        .orElseThrow(() -> new OnlineAuctionNotFoundException("Online auction not found"));
  }

  private OnlineAuction requirePublicAuction(String tenantId, String auctionId) {
    return auctionRepository
        .findPublicByTenantIdAndId(tenantId, auctionId)
        .orElseThrow(() -> new OnlineAuctionNotFoundException("Public online auction not found"));
  }

  private String normalizeSlug(String value) {
    return value.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
  }

  private ReviewCheckStatus parseCheckStatus(String value) {
    return switch (value.trim().toUpperCase()) {
      case "PASS", "PASSED" -> ReviewCheckStatus.PASSED;
      case "FAIL", "FAILED" -> ReviewCheckStatus.FAILED;
      case "PENDING" -> ReviewCheckStatus.PENDING;
      default ->
          throw new IllegalArgumentException("Check status must be PENDING, PASSED or FAILED");
    };
  }

  private void publish(String channel, String eventType, Object payload) {
    realtimePublisher.publish(channel, java.util.Map.of("type", eventType, "payload", payload));
  }

  private OnlineAuctionResponse toResponse(OnlineAuction auction) {
    return new OnlineAuctionResponse(
        auction.id(),
        auction.tenantId(),
        auction.title(),
        auction.slug(),
        auction.status(),
        auction.channelName(),
        auction.minimumIncrement(),
        auction.countdownSeconds(),
        auction.publishedAt(),
        auction.liveStartedAt(),
        auction.countdownEndsAt(),
        auction.closedAt(),
        auction.lots().stream().map(this::toLotResponse).toList(),
        auction.registrations().stream().map(this::toRegistrationResponse).toList());
  }

  private OnlineAuctionLotResponse toLotResponse(OnlineAuctionLot lot) {
    return new OnlineAuctionLotResponse(
        lot.id(),
        lot.lotNumber(),
        lot.title(),
        lot.description(),
        lot.startingBid(),
        lot.currentBid(),
        lot.highestBidderAlias());
  }

  private BidderRegistrationResponse toRegistrationResponse(BidderRegistration registration) {
    return new BidderRegistrationResponse(
        registration.id(),
        registration.displayName(),
        registration.email(),
        registration.legalName(),
        registration.birthDate(),
        maskIban(registration.iban()),
        registration.paddleNumber(),
        registration.accessToken(),
        registration.approvalStatus(),
        registration.kycStatus(),
        registration.accountCheckStatus(),
        registration.reviewNote(),
        registration.approvedAt(),
        registration.createdAt());
  }

  private String maskIban(String iban) {
    if (iban == null || iban.length() < 4) {
      return "****";
    }
    return "****" + iban.substring(Math.max(0, iban.length() - 4));
  }
}
