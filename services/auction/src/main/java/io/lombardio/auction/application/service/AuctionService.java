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
import java.time.Month;
import java.util.ArrayList;
import java.util.Comparator;
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
    return auctionRepository.findByTenantId(tenantId).stream()
        .sorted(Comparator.comparing(Auction::createdAt).reversed())
        .toList();
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
    if (request.auctionDate() == null) {
      throw new IllegalArgumentException("auctionDate is required");
    }
    if (request.auctionDate().isBefore(announcementDate.plusDays(7))
        || request.auctionDate().isAfter(announcementDate.plusDays(14))) {
      throw new IllegalArgumentException(
          "auctionDate must be between 7 and 14 days after public announcement");
    }

    Auction updated =
        new Auction(
            auction.id(),
            auction.tenantId(),
            auction.title(),
            auction.location(),
            AuctionStatus.ANNOUNCED,
            announcementDate,
            request.auctionDate(),
            auction.liveStartedAt(),
            auction.closedAt(),
            request.announcementReference(),
            auction.lots(),
            auction.createdAt(),
            Instant.now(clock));
    return auctionRepository.save(updated);
  }

  public Auction openAuction(String tenantId, String auctionId) {
    Auction auction = requireAuction(tenantId, auctionId);
    if (auction.status() != AuctionStatus.ANNOUNCED) {
      throw new IllegalArgumentException("Only announced auctions can be opened");
    }
    List<AuctionLot> lots =
        auction.lots().stream()
            .map(
                lot ->
                    new AuctionLot(
                        lot.id(),
                        lot.auctionId(),
                        lot.lotNumber(),
                        lot.contractNumber(),
                        lot.itemNumber(),
                        lot.description(),
                        lot.estimatedValue(),
                        lot.outstandingClaim(),
                        lot.latestBidAmount(),
                        lot.leadingBidder(),
                        lot.hammerPrice(),
                        AuctionLotStatus.OPEN,
                        lot.surplusAmount(),
                        lot.authorityTransferDueDate(),
                        lot.authorityTransferStatus()))
            .toList();
    Auction updated =
        new Auction(
            auction.id(),
            auction.tenantId(),
            auction.title(),
            auction.location(),
            AuctionStatus.LIVE,
            auction.publicAnnouncementDate(),
            auction.auctionDate(),
            Instant.now(clock),
            auction.closedAt(),
            auction.announcementReference(),
            lots,
            auction.createdAt(),
            Instant.now(clock));
    return auctionRepository.save(updated);
  }

  public Auction closeAuction(String tenantId, String auctionId) {
    Auction auction = requireAuction(tenantId, auctionId);
    Auction updated =
        new Auction(
            auction.id(),
            auction.tenantId(),
            auction.title(),
            auction.location(),
            AuctionStatus.CLOSED,
            auction.publicAnnouncementDate(),
            auction.auctionDate(),
            auction.liveStartedAt(),
            Instant.now(clock),
            auction.announcementReference(),
            auction.lots(),
            auction.createdAt(),
            Instant.now(clock));
    return auctionRepository.save(updated);
  }

  public Auction placeBid(
      String tenantId, String auctionId, String lotId, PlaceBidCommand request) {
    Auction auction = requireAuction(tenantId, auctionId);
    if (auction.status() != AuctionStatus.LIVE) {
      throw new IllegalArgumentException("Auction must be live before bids can be placed");
    }
    List<AuctionLot> lots =
        auction.lots().stream().map(lot -> updateBid(lot, lotId, request)).toList();
    Auction updated =
        new Auction(
            auction.id(),
            auction.tenantId(),
            auction.title(),
            auction.location(),
            auction.status(),
            auction.publicAnnouncementDate(),
            auction.auctionDate(),
            auction.liveStartedAt(),
            auction.closedAt(),
            auction.announcementReference(),
            lots,
            auction.createdAt(),
            Instant.now(clock));
    return auctionRepository.save(updated);
  }

  public Auction settleLot(
      String tenantId, String auctionId, String lotId, SettleAuctionLotCommand request) {
    Auction auction = requireAuction(tenantId, auctionId);
    List<AuctionLot> lots =
        auction.lots().stream().map(lot -> settle(lot, lotId, request.hammerPrice())).toList();
    Auction updated =
        new Auction(
            auction.id(),
            auction.tenantId(),
            auction.title(),
            auction.location(),
            auction.status(),
            auction.publicAnnouncementDate(),
            auction.auctionDate(),
            auction.liveStartedAt(),
            auction.closedAt(),
            auction.announcementReference(),
            lots,
            auction.createdAt(),
            Instant.now(clock));
    return auctionRepository.save(updated);
  }

  public List<SurplusCase> listSurplusCases(String tenantId) {
    return auctionRepository.findByTenantId(tenantId).stream()
        .flatMap(auction -> auction.lots().stream())
        .filter(
            lot ->
                lot.surplusAmount() != null && lot.surplusAmount().compareTo(BigDecimal.ZERO) > 0)
        .map(
            lot ->
                new SurplusCase(
                    lot.auctionId(),
                    lot.id(),
                    lot.lotNumber(),
                    lot.contractNumber(),
                    lot.hammerPrice(),
                    lot.outstandingClaim(),
                    lot.surplusAmount(),
                    lot.authorityTransferDueDate(),
                    lot.authorityTransferStatus()))
        .toList();
  }

  private AuctionLot updateBid(AuctionLot lot, String lotId, PlaceBidCommand request) {
    if (!lot.id().equals(lotId)) {
      return lot;
    }
    BigDecimal latest = lot.latestBidAmount() == null ? BigDecimal.ZERO : lot.latestBidAmount();
    if (request.amount().compareTo(latest) <= 0) {
      throw new IllegalArgumentException("Bid amount must be higher than the current bid");
    }
    return new AuctionLot(
        lot.id(),
        lot.auctionId(),
        lot.lotNumber(),
        lot.contractNumber(),
        lot.itemNumber(),
        lot.description(),
        lot.estimatedValue(),
        lot.outstandingClaim(),
        request.amount(),
        request.bidderDisplayName(),
        lot.hammerPrice(),
        AuctionLotStatus.OPEN,
        lot.surplusAmount(),
        lot.authorityTransferDueDate(),
        lot.authorityTransferStatus());
  }

  private AuctionLot settle(AuctionLot lot, String lotId, BigDecimal hammerPrice) {
    if (!lot.id().equals(lotId)) {
      return lot;
    }
    BigDecimal surplus = hammerPrice.subtract(lot.outstandingClaim()).max(BigDecimal.ZERO);
    LocalDate transferDueDate =
        surplus.compareTo(BigDecimal.ZERO) > 0
            ? LocalDate.of(LocalDate.now(clock).getYear(), Month.DECEMBER, 31)
                .plusYears(3)
                .plusMonths(1)
            : null;
    return new AuctionLot(
        lot.id(),
        lot.auctionId(),
        lot.lotNumber(),
        lot.contractNumber(),
        lot.itemNumber(),
        lot.description(),
        lot.estimatedValue(),
        lot.outstandingClaim(),
        lot.latestBidAmount(),
        lot.leadingBidder(),
        hammerPrice,
        hammerPrice.compareTo(BigDecimal.ZERO) > 0
            ? AuctionLotStatus.SOLD
            : AuctionLotStatus.UNSOLD,
        surplus,
        transferDueDate,
        surplus.compareTo(BigDecimal.ZERO) > 0 ? "OPEN" : "NOT_APPLICABLE");
  }

  private Auction requireAuction(String tenantId, String auctionId) {
    return auctionRepository
        .findByTenantIdAndId(tenantId, auctionId)
        .orElseThrow(() -> new AuctionNotFoundException("Auction not found: " + auctionId));
  }
}
