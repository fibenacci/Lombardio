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
      OnlineAuctionMetrics metrics) {
    this.auctionRepository = auctionRepository;
    this.lifecycleService = lifecycleService;
    this.realtimePublisher = realtimePublisher;
    this.metrics = metrics;
    this.mapper = new OnlineAuctionMapper();
  }

  public BidderRegistrationResponse registerBidder(
      String tenantId, String auctionId, RegisterBidderRequest request) {
    OnlineAuction current = lifecycleService.requirePublicAuction(tenantId, auctionId);
    String rawAccessToken = UUID.randomUUID().toString().replace("-", "");
    BidderRegistration persistedRegistration =
        OnlineAuctionMutations.createPersistedRegistration(
            current, request, rawAccessToken, Instant.now());
    BidderRegistration responseRegistration =
        OnlineAuctionMutations.exposeAccessToken(persistedRegistration, rawAccessToken);
    OnlineAuction updated =
        OnlineAuctionMutations.appendRegistration(current, persistedRegistration, Instant.now());
    OnlineAuction saved = auctionRepository.save(updated);
    publishEvent(
        saved.channelName(),
        "bidder_registered",
        mapper.toRegistrationResponse(persistedRegistration, false));
    metrics.recordBidderRegistration();
    return mapper.toRegistrationResponse(responseRegistration, true);
  }

  public OnlineAuctionResponse reviewRegistration(
      String tenantId, String auctionId, String registrationId, BidderReviewRequest request) {
    OnlineAuction current = lifecycleService.requireAuction(tenantId, auctionId);
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
    publishEvent(saved.channelName(), "registration_reviewed", mapper.toAdminResponse(saved));
    metrics.recordBidderReview(status, kycStatus, accountCheckStatus);
    return mapper.toAdminResponse(saved);
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

  private void publishEvent(String channel, String eventType, Object payload) {
    realtimePublisher.publish(channel, Map.of("type", eventType, "payload", payload));
  }
}
