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
package io.lombardio.onlineauction.api;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.lombardio.onlineauction.application.OnlineAuctionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/tenants/{tenantId}/online-auctions")
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring managed singleton beans")
public class PublicOnlineAuctionController {

  private final OnlineAuctionService onlineAuctionService;

  public PublicOnlineAuctionController(OnlineAuctionService onlineAuctionService) {
    this.onlineAuctionService = onlineAuctionService;
  }

  @GetMapping
  List<OnlineAuctionResponse> list(@PathVariable String tenantId) {
    return onlineAuctionService.listPublicAuctions(tenantId);
  }

  @GetMapping("/{auctionId}")
  OnlineAuctionResponse get(@PathVariable String tenantId, @PathVariable String auctionId) {
    return onlineAuctionService.getPublicAuction(tenantId, auctionId);
  }

  @PostMapping("/{auctionId}/registrations")
  @ResponseStatus(HttpStatus.CREATED)
  BidderRegistrationResponse register(
      @PathVariable String tenantId,
      @PathVariable String auctionId,
      @Valid @RequestBody RegisterBidderRequest request) {
    return onlineAuctionService.registerBidder(tenantId, auctionId, request);
  }

  @PostMapping("/{auctionId}/bids")
  OnlineAuctionResponse placeBid(
      @PathVariable String tenantId,
      @PathVariable String auctionId,
      @Valid @RequestBody PlaceOnlineBidRequest request) {
    return onlineAuctionService.placeBid(tenantId, auctionId, request);
  }

  @PostMapping("/{auctionId}/realtime-session")
  RealtimeSessionResponse issueRealtimeSession(
      @PathVariable String tenantId,
      @PathVariable String auctionId,
      @Valid @RequestBody RealtimeSessionRequest request) {
    return onlineAuctionService.issueRealtimeSession(tenantId, auctionId, request.accessToken());
  }
}
