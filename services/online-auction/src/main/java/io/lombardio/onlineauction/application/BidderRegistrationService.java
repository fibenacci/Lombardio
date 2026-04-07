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
import io.lombardio.onlineauction.api.OnlineAuctionResponse;
import io.lombardio.onlineauction.api.RegisterBidderRequest;
import io.lombardio.onlineauction.domain.BidderApprovalStatus;
import io.lombardio.onlineauction.domain.BidderRegistration;
import io.lombardio.onlineauction.domain.OnlineAuction;
import io.lombardio.onlineauction.domain.OnlineAuctionRepository;
import io.lombardio.onlineauction.domain.RealtimePublisher;
import io.lombardio.onlineauction.domain.ReviewCheckStatus;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class BidderRegistrationService {

  private final OnlineAuctionRepository auctionRepository;
  private final OnlineAuctionLifecycleService lifecycleService;
  private final RealtimePublisher realtimePublisher;
  private final OnlineAuctionMetrics metrics;
  private final OnlineAuctionMapper mapper;

  public BidderRegistrationService(
      OnlineAuctionRepository auctionRepository,
      OnlineAuctionLifecycleService lifecycleService,
      RealtimePublisher realtimePublisher,
      OnlineAuctionMetrics metrics,
      OnlineAuctionMapper mapper) {
    this.auctionRepository = auctionRepository;
    this.lifecycleService = lifecycleService;
    this.realtimePublisher = realtimePublisher;
    this.metrics = metrics;
    this.mapper = mapper;
  }

  public BidderRegistrationResponse registerBidder(
      String tenantId, String auctionId, RegisterBidderRequest request) {
    OnlineAuction current = lifecycleService.requirePublicAuction(tenantId, auctionId);
    String rawAccessToken = UUID.randomUUID().toString().replace("-", "");
    String hashedToken = BidderAccessTokenHasher.sha256(rawAccessToken);

    BidderRegistration persisted =
        BidderRegistration.create(current, request, null, hashedToken, Instant.now());
    OnlineAuction updated = current.appendRegistration(persisted, Instant.now());

    OnlineAuction saved = auctionRepository.save(updated);
    publishEvent(
        saved.channelName(), "bidder_registered", mapper.toRegistrationResponse(persisted, false));
    metrics.recordBidderRegistration();

    return mapper.toRegistrationResponse(persisted.withAccessToken(rawAccessToken), true);
  }

  public OnlineAuctionResponse reviewRegistration(
      String tenantId, String auctionId, String registrationId, BidderReviewRequest request) {
    OnlineAuction current = lifecycleService.requireAuction(tenantId, auctionId);
    ReviewCheckStatus kycStatus = parseCheckStatus(request.kycStatus());
    ReviewCheckStatus accountCheckStatus = parseCheckStatus(request.accountCheckStatus());
    BidderApprovalStatus decision = parseDecision(request.decision());

    if (decision == BidderApprovalStatus.APPROVED
        && (kycStatus != ReviewCheckStatus.PASSED
            || accountCheckStatus != ReviewCheckStatus.PASSED)) {
      throw new IllegalArgumentException("Bidder approval requires passed KYC and account checks");
    }

    BidderRegistration target =
        current.registrations().stream()
            .filter(r -> r.id().equals(registrationId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Registration not found"));

    BidderRegistration reviewed =
        target.review(decision, kycStatus, accountCheckStatus, request.reviewNote(), Instant.now());
    OnlineAuction updated = current.reviewRegistration(registrationId, reviewed, Instant.now());

    OnlineAuction saved = auctionRepository.save(updated);
    publishEvent(saved.channelName(), "registration_reviewed", mapper.toAdminResponse(saved));
    metrics.recordBidderReview(decision, kycStatus, accountCheckStatus);
    return mapper.toAdminResponse(saved);
  }

  private ReviewCheckStatus parseCheckStatus(String value) {
    String trimmedValue = value == null ? "" : value.trim();
    if ("PASS".equals(trimmedValue) || "PASSED".equals(trimmedValue)) {
      return ReviewCheckStatus.PASSED;
    }
    if ("FAIL".equals(trimmedValue) || "FAILED".equals(trimmedValue)) {
      return ReviewCheckStatus.FAILED;
    }
    if ("PENDING".equals(trimmedValue)) {
      return ReviewCheckStatus.PENDING;
    }
    throw new IllegalArgumentException("Check status must be PENDING, PASSED or FAILED");
  }

  private BidderApprovalStatus parseDecision(String value) {
    String trimmedValue = value == null ? "" : value.trim();
    if ("APPROVE".equals(trimmedValue) || "APPROVED".equals(trimmedValue)) {
      return BidderApprovalStatus.APPROVED;
    }
    if ("REJECT".equals(trimmedValue) || "REJECTED".equals(trimmedValue)) {
      return BidderApprovalStatus.REJECTED;
    }
    throw new IllegalArgumentException("Decision must be APPROVE or REJECT");
  }

  private void publishEvent(String channel, String eventType, Object payload) {
    realtimePublisher.publish(channel, Map.of("type", eventType, "payload", payload));
  }
}
