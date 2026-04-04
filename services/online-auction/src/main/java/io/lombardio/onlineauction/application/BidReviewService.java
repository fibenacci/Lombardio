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

import io.lombardio.onlineauction.api.OnlineAuctionResponse;
import io.lombardio.onlineauction.api.PlaceOnlineBidRequest;
import io.lombardio.onlineauction.domain.BidderApprovalStatus;
import io.lombardio.onlineauction.domain.BidderRegistration;
import io.lombardio.onlineauction.domain.OnlineAuction;
import io.lombardio.onlineauction.domain.OnlineAuctionRepository;
import io.lombardio.onlineauction.domain.OnlineAuctionStatus;
import io.lombardio.onlineauction.domain.RealtimePublisher;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class BidReviewService {

  private final OnlineAuctionRepository auctionRepository;
  private final OnlineAuctionLifecycleService lifecycleService;
  private final RealtimePublisher realtimePublisher;
  private final OnlineAuctionMetrics metrics;
  private final OnlineAuctionMapper mapper;

  public BidReviewService(
      OnlineAuctionRepository auctionRepository,
      OnlineAuctionLifecycleService lifecycleService,
      RealtimePublisher realtimePublisher,
      OnlineAuctionMetrics metrics) {
    this.auctionRepository = auctionRepository;
    this.lifecycleService = lifecycleService;
    this.realtimePublisher = realtimePublisher;
    this.metrics = metrics;
    this.mapper = new OnlineAuctionMapper();
  }

  public OnlineAuctionResponse placeBid(
      String tenantId, String auctionId, PlaceOnlineBidRequest request) {
    OnlineAuction current = lifecycleService.requirePublicAuction(tenantId, auctionId);
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
    publishEvent(saved.channelName(), "bid_placed", mapper.toAdminResponse(saved));
    metrics.recordBidPlaced(request.amount());
    return mapper.toPublicResponse(saved);
  }

  private void publishEvent(String channel, String eventType, Object payload) {
    realtimePublisher.publish(channel, Map.of("type", eventType, "payload", payload));
  }

  private boolean bidderMatchesAccessToken(BidderRegistration bidder, String rawAccessToken) {
    String candidateHash = BidderAccessTokenHasher.sha256(rawAccessToken);
    if (bidder.accessTokenHash() != null) {
      return MessageDigest.isEqual(
          bidder.accessTokenHash().getBytes(java.nio.charset.StandardCharsets.UTF_8),
          candidateHash.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
    return MessageDigest.isEqual(
        bidder.accessToken().getBytes(java.nio.charset.StandardCharsets.UTF_8),
        rawAccessToken.getBytes(java.nio.charset.StandardCharsets.UTF_8));
  }
}
