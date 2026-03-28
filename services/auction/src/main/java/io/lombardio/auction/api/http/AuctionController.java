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

import io.lombardio.auction.application.service.AnnounceAuctionCommand;
import io.lombardio.auction.application.service.AuctionService;
import io.lombardio.auction.application.service.CreateAuctionCommand;
import io.lombardio.auction.application.service.CreateAuctionLotCommand;
import io.lombardio.auction.application.service.PlaceBidCommand;
import io.lombardio.auction.application.service.SettleAuctionLotCommand;
import io.lombardio.auction.application.service.SurplusCase;
import io.lombardio.auction.domain.model.Auction;
import io.lombardio.auction.domain.model.AuctionLot;
import io.lombardio.auction.infrastructure.security.AuctionAuthorizationService;
import io.lombardio.platform.security.AuthenticatedUser;
import jakarta.validation.Valid;
import java.util.List;
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
public class AuctionController {

  private final AuctionService auctionService;
  private final AuctionAuthorizationService authorizationService;

  public AuctionController(
      AuctionService auctionService, AuctionAuthorizationService authorizationService) {
    this.auctionService = auctionService;
    this.authorizationService = authorizationService;
  }

  @GetMapping("/auctions")
  List<AuctionResponse> listAuctions(
      @PathVariable String tenantId, @AuthenticationPrincipal AuthenticatedUser user) {
    authorizationService.requireRead(user, tenantId);
    return auctionService.listAuctions(tenantId).stream().map(this::toResponse).toList();
  }

  @PostMapping("/auctions")
  @ResponseStatus(HttpStatus.CREATED)
  AuctionResponse createAuction(
      @PathVariable String tenantId,
      @Valid @RequestBody CreateAuctionRequest request,
      @AuthenticationPrincipal AuthenticatedUser user) {
    authorizationService.requireWrite(user, tenantId);
    return toResponse(auctionService.createAuction(tenantId, toCommand(request)));
  }

  @PostMapping("/auctions/{auctionId}/announce")
  AuctionResponse announceAuction(
      @PathVariable String tenantId,
      @PathVariable String auctionId,
      @Valid @RequestBody AuctionStatusUpdateRequest request,
      @AuthenticationPrincipal AuthenticatedUser user) {
    authorizationService.requireWrite(user, tenantId);
    return toResponse(auctionService.announceAuction(tenantId, auctionId, toCommand(request)));
  }

  @PostMapping("/auctions/{auctionId}/open")
  AuctionResponse openAuction(
      @PathVariable String tenantId,
      @PathVariable String auctionId,
      @AuthenticationPrincipal AuthenticatedUser user) {
    authorizationService.requireWrite(user, tenantId);
    return toResponse(auctionService.openAuction(tenantId, auctionId));
  }

  @PostMapping("/auctions/{auctionId}/close")
  AuctionResponse closeAuction(
      @PathVariable String tenantId,
      @PathVariable String auctionId,
      @AuthenticationPrincipal AuthenticatedUser user) {
    authorizationService.requireWrite(user, tenantId);
    return toResponse(auctionService.closeAuction(tenantId, auctionId));
  }

  @PostMapping("/auctions/{auctionId}/lots/{lotId}/bids")
  AuctionResponse placeBid(
      @PathVariable String tenantId,
      @PathVariable String auctionId,
      @PathVariable String lotId,
      @Valid @RequestBody PlaceBidRequest request,
      @AuthenticationPrincipal AuthenticatedUser user) {
    authorizationService.requireWrite(user, tenantId);
    return toResponse(auctionService.placeBid(tenantId, auctionId, lotId, toCommand(request)));
  }

  @PostMapping("/auctions/{auctionId}/lots/{lotId}/settle")
  AuctionResponse settleLot(
      @PathVariable String tenantId,
      @PathVariable String auctionId,
      @PathVariable String lotId,
      @Valid @RequestBody AuctionSettlementRequest request,
      @AuthenticationPrincipal AuthenticatedUser user) {
    authorizationService.requireWrite(user, tenantId);
    return toResponse(auctionService.settleLot(tenantId, auctionId, lotId, toCommand(request)));
  }

  @GetMapping("/surplus-cases")
  List<SurplusCaseResponse> listSurplusCases(
      @PathVariable String tenantId, @AuthenticationPrincipal AuthenticatedUser user) {
    authorizationService.requireRead(user, tenantId);
    return auctionService.listSurplusCases(tenantId).stream().map(this::toResponse).toList();
  }

  private CreateAuctionCommand toCommand(CreateAuctionRequest request) {
    return new CreateAuctionCommand(
        request.title(), request.location(), request.lots().stream().map(this::toCommand).toList());
  }

  private CreateAuctionLotCommand toCommand(AuctionLotRequest request) {
    return new CreateAuctionLotCommand(
        request.contractNumber(),
        request.itemNumber(),
        request.description(),
        request.estimatedValue(),
        request.outstandingClaim());
  }

  private AnnounceAuctionCommand toCommand(AuctionStatusUpdateRequest request) {
    return new AnnounceAuctionCommand(request.auctionDate(), request.announcementReference());
  }

  private PlaceBidCommand toCommand(PlaceBidRequest request) {
    return new PlaceBidCommand(request.bidderDisplayName(), request.amount());
  }

  private SettleAuctionLotCommand toCommand(AuctionSettlementRequest request) {
    return new SettleAuctionLotCommand(request.hammerPrice());
  }

  private AuctionResponse toResponse(Auction auction) {
    return new AuctionResponse(
        auction.id(),
        auction.title(),
        auction.location(),
        auction.status(),
        auction.publicAnnouncementDate(),
        auction.auctionDate(),
        auction.liveStartedAt(),
        auction.closedAt(),
        auction.announcementReference(),
        auction.lots().stream().map(this::toResponse).toList());
  }

  private AuctionLotResponse toResponse(AuctionLot lot) {
    return new AuctionLotResponse(
        lot.id(),
        lot.lotNumber(),
        lot.contractNumber(),
        lot.itemNumber(),
        lot.description(),
        lot.estimatedValue(),
        lot.outstandingClaim(),
        lot.latestBidAmount(),
        lot.leadingBidder(),
        lot.hammerPrice(),
        lot.status(),
        lot.surplusAmount(),
        lot.authorityTransferDueDate(),
        lot.authorityTransferStatus());
  }

  private SurplusCaseResponse toResponse(SurplusCase surplusCase) {
    return new SurplusCaseResponse(
        surplusCase.auctionId(),
        surplusCase.lotId(),
        surplusCase.lotNumber(),
        surplusCase.contractNumber(),
        surplusCase.hammerPrice(),
        surplusCase.outstandingClaim(),
        surplusCase.surplusAmount(),
        surplusCase.authorityTransferDueDate(),
        surplusCase.authorityTransferStatus());
  }
}
