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
import io.lombardio.onlineauction.api.OnlineAuctionResponse;
import io.lombardio.onlineauction.api.PlaceOnlineBidRequest;
import io.lombardio.onlineauction.api.RealtimeSessionResponse;
import io.lombardio.onlineauction.api.RegisterBidderRequest;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OnlineAuctionService {

  private final OnlineAuctionLifecycleService lifecycleService;
  private final BidderRegistrationService registrationService;
  private final BidReviewService bidReviewService;
  private final RealtimeSessionService realtimeSessionService;

  public OnlineAuctionService(
      OnlineAuctionLifecycleService lifecycleService,
      BidderRegistrationService registrationService,
      BidReviewService bidReviewService,
      RealtimeSessionService realtimeSessionService) {
    this.lifecycleService = lifecycleService;
    this.registrationService = registrationService;
    this.bidReviewService = bidReviewService;
    this.realtimeSessionService = realtimeSessionService;
  }

  public List<OnlineAuctionResponse> listAdminAuctions(String tenantId) {
    return lifecycleService.listAdminAuctions(tenantId);
  }

  public OnlineAuctionResponse createAuction(String tenantId, CreateOnlineAuctionRequest request) {
    return lifecycleService.createAuction(tenantId, request);
  }

  public OnlineAuctionResponse publishAuction(String tenantId, String auctionId) {
    return lifecycleService.publishAuction(tenantId, auctionId);
  }

  public OnlineAuctionResponse startAuction(String tenantId, String auctionId) {
    return lifecycleService.startAuction(tenantId, auctionId);
  }

  public OnlineAuctionResponse closeAuction(String tenantId, String auctionId) {
    return lifecycleService.closeAuction(tenantId, auctionId);
  }

  public List<OnlineAuctionResponse> listPublicAuctions(String tenantId) {
    return lifecycleService.listPublicAuctions(tenantId);
  }

  public OnlineAuctionResponse getPublicAuction(String tenantId, String auctionId) {
    return lifecycleService.getPublicAuction(tenantId, auctionId);
  }

  public io.lombardio.onlineauction.api.BidderRegistrationResponse registerBidder(
      String tenantId, String auctionId, RegisterBidderRequest request) {
    return registrationService.registerBidder(tenantId, auctionId, request);
  }

  public OnlineAuctionResponse reviewRegistration(
      String tenantId, String auctionId, String registrationId, BidderReviewRequest request) {
    return registrationService.reviewRegistration(tenantId, auctionId, registrationId, request);
  }

  public OnlineAuctionResponse placeBid(
      String tenantId, String auctionId, PlaceOnlineBidRequest request) {
    return bidReviewService.placeBid(tenantId, auctionId, request);
  }

  public RealtimeSessionResponse issueRealtimeSession(
      String tenantId, String auctionId, String accessToken) {
    return realtimeSessionService.issueRealtimeSession(tenantId, auctionId, accessToken);
  }
}
