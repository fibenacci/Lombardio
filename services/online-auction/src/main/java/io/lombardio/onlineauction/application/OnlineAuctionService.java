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
import io.lombardio.onlineauction.api.OnlineAuctionNotFoundException;
import io.lombardio.onlineauction.api.OnlineAuctionResponse;
import io.lombardio.onlineauction.api.PlaceOnlineBidRequest;
import io.lombardio.onlineauction.api.RealtimeSessionResponse;
import io.lombardio.onlineauction.api.RegisterBidderRequest;
import io.lombardio.onlineauction.domain.BidderApprovalStatus;
import io.lombardio.onlineauction.domain.BidderRegistration;
import io.lombardio.onlineauction.domain.OnlineAuction;
import io.lombardio.onlineauction.domain.OnlineAuctionRepository;
import io.lombardio.onlineauction.domain.OnlineAuctionStatus;
import io.lombardio.onlineauction.domain.RealtimePublisher;
import io.lombardio.onlineauction.domain.RealtimeSession;
import io.lombardio.onlineauction.domain.RealtimeSessionTokenService;
import io.lombardio.onlineauction.domain.ReviewCheckStatus;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OnlineAuctionService {

  private final OnlineAuctionRepository auctionRepository;
  private final RealtimePublisher realtimePublisher;
  private final RealtimeSessionTokenService realtimeSessionTokenService;
  private final OnlineAuctionMetrics metrics;
  private final OnlineAuctionMapper mapper;

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
    this.mapper = new OnlineAuctionMapper();
  }

  public List<OnlineAuctionResponse> listAdminAuctions(String tenantId) {
    return auctionRepository.findByTenantId(tenantId).stream()
        .sorted(Comparator.comparing(OnlineAuction::createdAt).reversed())
        .map(mapper::toAdminResponse)
        .toList();
  }

  public OnlineAuctionResponse createAuction(String tenantId, CreateOnlineAuctionRequest request) {
    OnlineAuction saved =
        auctionRepository.save(
            OnlineAuctionMutations.createDraftAuction(tenantId, request, Instant.now()));
    metrics.recordAuctionCreated();
    return mapper.toAdminResponse(saved);
  }

  public OnlineAuctionResponse publishAuction(String tenantId, String auctionId) {
    OnlineAuction current = requireAuction(tenantId, auctionId);
    if (current.status() != OnlineAuctionStatus.DRAFT) {
      throw new IllegalArgumentException("Only draft auctions can be published");
    }
    OnlineAuction updated = OnlineAuctionMutations.publish(current, Instant.now());
    OnlineAuction saved = auctionRepository.save(updated);
    publish(saved.channelName(), "auction_published", saved);
    return mapper.toAdminResponse(saved);
  }

  public OnlineAuctionResponse startAuction(String tenantId, String auctionId) {
    OnlineAuction current = requireAuction(tenantId, auctionId);
    if (current.status() != OnlineAuctionStatus.PUBLISHED) {
      throw new IllegalArgumentException("Only published auctions can go live");
    }
    OnlineAuction updated = OnlineAuctionMutations.start(current, Instant.now());
    OnlineAuction saved = auctionRepository.save(updated);
    publish(saved.channelName(), "auction_live", saved);
    return mapper.toAdminResponse(saved);
  }

  public OnlineAuctionResponse closeAuction(String tenantId, String auctionId) {
    OnlineAuction current = requireAuction(tenantId, auctionId);
    if (current.status() != OnlineAuctionStatus.LIVE) {
      throw new IllegalArgumentException("Only live auctions can be closed");
    }
    OnlineAuction updated = OnlineAuctionMutations.close(current, Instant.now());
    OnlineAuction saved = auctionRepository.save(updated);
    publish(saved.channelName(), "auction_closed", saved);
    return mapper.toAdminResponse(saved);
  }

  public List<OnlineAuctionResponse> listPublicAuctions(String tenantId) {
    return auctionRepository.findPublicByTenantId(tenantId).stream()
        .map(mapper::toPublicResponse)
        .toList();
  }

  public OnlineAuctionResponse getPublicAuction(String tenantId, String auctionId) {
    return mapper.toPublicResponse(requirePublicAuction(tenantId, auctionId));
  }

  public io.lombardio.onlineauction.api.BidderRegistrationResponse registerBidder(
      String tenantId, String auctionId, RegisterBidderRequest request) {
    OnlineAuction current = requirePublicAuction(tenantId, auctionId);
    String rawAccessToken = java.util.UUID.randomUUID().toString().replace("-", "");
    BidderRegistration persistedRegistration =
        OnlineAuctionMutations.createPersistedRegistration(
            current, request, rawAccessToken, Instant.now());
    BidderRegistration responseRegistration =
        OnlineAuctionMutations.exposeAccessToken(persistedRegistration, rawAccessToken);
    OnlineAuction updated =
        OnlineAuctionMutations.appendRegistration(current, persistedRegistration, Instant.now());
    OnlineAuction saved = auctionRepository.save(updated);
    publish(
        saved.channelName(),
        "bidder_registered",
        mapper.toRegistrationResponse(persistedRegistration, false));
    metrics.recordBidderRegistration();
    return mapper.toRegistrationResponse(responseRegistration, true);
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
    OnlineAuction updated =
        OnlineAuctionMutations.reviewRegistration(
            current,
            registrationId,
            status,
            kycStatus,
            accountCheckStatus,
            request,
            status == BidderApprovalStatus.APPROVED ? Instant.now() : null,
            Instant.now());
    OnlineAuction saved = auctionRepository.save(updated);
    publish(saved.channelName(), "registration_reviewed", mapper.toAdminResponse(saved));
    metrics.recordBidderReview(status, kycStatus, accountCheckStatus);
    return mapper.toAdminResponse(saved);
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
            .filter(item -> bidderMatchesAccessToken(item, request.accessToken()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown bidder session"));
    if (bidder.approvalStatus() != BidderApprovalStatus.APPROVED) {
      throw new IllegalArgumentException("Bidder is not approved for live bidding");
    }
    OnlineAuction updated =
        OnlineAuctionMutations.applyBid(current, request, bidder, Instant.now());
    OnlineAuction saved = auctionRepository.save(updated);
    publish(saved.channelName(), "bid_placed", mapper.toAdminResponse(saved));
    metrics.recordBidPlaced(request.amount());
    return mapper.toPublicResponse(saved);
  }

  public RealtimeSessionResponse issueRealtimeSession(
      String tenantId, String auctionId, String accessToken) {
    OnlineAuction auction = requirePublicAuction(tenantId, auctionId);
    BidderRegistration bidder =
        auction.registrations().stream()
            .filter(item -> bidderMatchesAccessToken(item, accessToken))
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

  private boolean bidderMatchesAccessToken(BidderRegistration bidder, String rawAccessToken) {
    String candidateHash = BidderAccessTokenHasher.sha256(rawAccessToken);
    return bidder.accessTokenHash() != null
        ? bidder.accessTokenHash().equals(candidateHash)
        : rawAccessToken.equals(bidder.accessToken());
  }
}
