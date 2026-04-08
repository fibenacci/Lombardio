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
package io.lombardio.auction.api.http;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.lombardio.auction.api.http.mapper.ApiMapper;
import io.lombardio.auction.application.service.AuctionService;
import io.lombardio.auction.infrastructure.security.AuctionAuthorizationService;
import io.lombardio.platform.security.AuthenticatedUser;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}")
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring managed singleton beans")
public class AuctionController {

  private final AuctionService auctionService;
  private final AuctionAuthorizationService authorizationService;
  private final ApiMapper mapper;

  @GetMapping("/auctions")
  public List<AuctionResponse> listAuctions(
      @PathVariable String tenantId, @AuthenticationPrincipal AuthenticatedUser user) {
    authorizationService.requireRead(user, tenantId);
    return auctionService.listAuctions(tenantId).stream().map(mapper::toResponse).toList();
  }

  @PostMapping("/auctions")
  @ResponseStatus(HttpStatus.CREATED)
  public AuctionResponse createAuction(
      @PathVariable String tenantId,
      @Valid @RequestBody CreateAuctionRequest request,
      @AuthenticationPrincipal AuthenticatedUser user) {
    authorizationService.requireWrite(user, tenantId);
    return mapper.toResponse(auctionService.createAuction(tenantId, mapper.toCommand(request)));
  }

  @PostMapping("/auctions/{auctionId}/announce")
  public AuctionResponse announceAuction(
      @PathVariable String tenantId,
      @PathVariable String auctionId,
      @Valid @RequestBody AuctionStatusUpdateRequest request,
      @AuthenticationPrincipal AuthenticatedUser user) {
    authorizationService.requireWrite(user, tenantId);
    return mapper.toResponse(
        auctionService.announceAuction(tenantId, auctionId, mapper.toCommand(request)));
  }

  @PostMapping("/auctions/{auctionId}/open")
  public AuctionResponse openAuction(
      @PathVariable String tenantId,
      @PathVariable String auctionId,
      @AuthenticationPrincipal AuthenticatedUser user) {
    authorizationService.requireWrite(user, tenantId);
    return mapper.toResponse(auctionService.openAuction(tenantId, auctionId));
  }

  @PostMapping("/auctions/{auctionId}/close")
  public AuctionResponse closeAuction(
      @PathVariable String tenantId,
      @PathVariable String auctionId,
      @AuthenticationPrincipal AuthenticatedUser user) {
    authorizationService.requireWrite(user, tenantId);
    return mapper.toResponse(auctionService.closeAuction(tenantId, auctionId));
  }

  @PostMapping("/auctions/{auctionId}/lots/{lotId}/bids")
  public AuctionResponse placeBid(
      @PathVariable String tenantId,
      @PathVariable String auctionId,
      @PathVariable String lotId,
      @Valid @RequestBody PlaceBidRequest request,
      @AuthenticationPrincipal AuthenticatedUser user) {
    authorizationService.requireWrite(user, tenantId);
    return mapper.toResponse(
        auctionService.placeBid(tenantId, auctionId, lotId, mapper.toCommand(request)));
  }

  @PostMapping("/auctions/{auctionId}/lots/{lotId}/settle")
  public AuctionResponse settleLot(
      @PathVariable String tenantId,
      @PathVariable String auctionId,
      @PathVariable String lotId,
      @Valid @RequestBody AuctionSettlementRequest request,
      @AuthenticationPrincipal AuthenticatedUser user) {
    authorizationService.requireWrite(user, tenantId);
    return mapper.toResponse(
        auctionService.settleLot(tenantId, auctionId, lotId, mapper.toCommand(request)));
  }

  @GetMapping("/surplus-cases")
  public List<SurplusCaseResponse> listSurplusCases(
      @PathVariable String tenantId, @AuthenticationPrincipal AuthenticatedUser user) {
    authorizationService.requireRead(user, tenantId);
    return auctionService.listSurplusCases(tenantId).stream()
        .map(mapper::toSurplusResponse)
        .toList();
  }
}
