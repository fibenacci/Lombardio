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
package io.lombardio.auction.application.service;

import io.lombardio.auction.domain.model.Auction;
import io.lombardio.auction.domain.model.AuctionLot;
import io.lombardio.auction.domain.model.AuctionLotStatus;
import io.lombardio.auction.domain.model.AuctionStatus;
import io.lombardio.auction.domain.port.AuctionRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AuctionService {

  private final AuctionRepository auctionRepository;
  private final Clock clock;

  public AuctionService(AuctionRepository auctionRepository, Clock clock) {
    this.auctionRepository = auctionRepository;
    this.clock = clock;
  }

  public List<Auction> listAuctions(String tenantId) {
    return auctionRepository.findByTenantId(tenantId);
  }

  public Auction createAuction(String tenantId, CreateAuctionCommand request) {
    Instant now = Instant.now(clock);
    String auctionId = "auction-" + UUID.randomUUID();
    List<AuctionLot> lots = new ArrayList<>();
    for (int index = 0; index < request.lots().size(); index++) {
      CreateAuctionLotCommand lotRequest = request.lots().get(index);
      lots.add(
          new AuctionLot(
              "lot-" + UUID.randomUUID(),
              auctionId,
              index + 1,
              lotRequest.contractNumber(),
              lotRequest.itemNumber(),
              lotRequest.description(),
              lotRequest.estimatedValue(),
              lotRequest.outstandingClaim(),
              BigDecimal.ZERO,
              null,
              null,
              AuctionLotStatus.PENDING,
              null,
              null,
              "NOT_DUE"));
    }

    Auction auction =
        new Auction(
            auctionId,
            tenantId,
            request.title(),
            request.location(),
            AuctionStatus.DRAFT,
            null,
            null,
            null,
            null,
            null,
            lots,
            now,
            now);
    return auctionRepository.save(auction);
  }

  public Auction announceAuction(
      String tenantId, String auctionId, AnnounceAuctionCommand request) {
    Auction auction = requireAuction(tenantId, auctionId);
    LocalDate announcementDate = LocalDate.now(clock);
    Auction updated =
        auction.announce(
            announcementDate,
            request.auctionDate(),
            request.announcementReference(),
            Instant.now(clock));
    return auctionRepository.save(updated);
  }

  public Auction openAuction(String tenantId, String auctionId) {
    Auction auction = requireAuction(tenantId, auctionId);
    Auction updated = auction.open(Instant.now(clock));
    return auctionRepository.save(updated);
  }

  public Auction closeAuction(String tenantId, String auctionId) {
    Auction auction = requireAuction(tenantId, auctionId);
    Auction updated = auction.close(Instant.now(clock));
    return auctionRepository.save(updated);
  }

  public Auction placeBid(
      String tenantId, String auctionId, String lotId, PlaceBidCommand request) {
    Auction auction = requireAuction(tenantId, auctionId);
    Auction updated =
        auction.placeBid(lotId, request.bidderDisplayName(), request.amount(), Instant.now(clock));
    return auctionRepository.save(updated);
  }

  public Auction settleLot(
      String tenantId, String auctionId, String lotId, SettleAuctionLotCommand request) {
    Auction auction = requireAuction(tenantId, auctionId);
    Auction updated =
        auction.settleLot(lotId, request.hammerPrice(), LocalDate.now(clock), Instant.now(clock));
    return auctionRepository.save(updated);
  }

  public List<SurplusCase> listSurplusCases(String tenantId) {
    return auctionRepository.findByTenantId(tenantId).stream()
        .flatMap(auction -> auction.lots().stream())
        .filter(
            lot ->
                lot.surplusAmount() != null && lot.surplusAmount().compareTo(BigDecimal.ZERO) > 0)
        .map(SurplusCase::from)
        .toList();
  }

  private Auction requireAuction(String tenantId, String auctionId) {
    return auctionRepository
        .findByTenantIdAndId(tenantId, auctionId)
        .orElseThrow(() -> new AuctionNotFoundException("Auction not found: " + auctionId));
  }
}
