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
package io.lombardio.auction.infrastructure.persistence.adapter;

import io.lombardio.auction.domain.model.Auction;
import io.lombardio.auction.domain.model.AuctionLot;
import io.lombardio.auction.domain.port.AuctionRepository;
import io.lombardio.auction.infrastructure.persistence.entity.AuctionEntity;
import io.lombardio.auction.infrastructure.persistence.entity.AuctionLotEntity;
import io.lombardio.auction.infrastructure.persistence.repository.SpringDataAuctionRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class AuctionPersistenceAdapter implements AuctionRepository {

  private final SpringDataAuctionRepository auctionRepository;

  public AuctionPersistenceAdapter(SpringDataAuctionRepository auctionRepository) {
    this.auctionRepository = auctionRepository;
  }

  @Override
  public List<Auction> findByTenantId(String tenantId) {
    return auctionRepository.findByTenantIdOrderByCreatedAtDesc(tenantId).stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public Optional<Auction> findByTenantIdAndId(String tenantId, String auctionId) {
    return auctionRepository.findByTenantIdAndId(tenantId, auctionId).map(this::toDomain);
  }

  @Override
  public Auction save(Auction auction) {
    return toDomain(auctionRepository.save(toEntity(auction)));
  }

  private AuctionEntity toEntity(Auction auction) {
    AuctionEntity entity = new AuctionEntity();
    entity.setId(auction.id());
    entity.setTenantId(auction.tenantId());
    entity.setTitle(auction.title());
    entity.setLocation(auction.location());
    entity.setStatus(auction.status());
    entity.setPublicAnnouncementDate(auction.publicAnnouncementDate());
    entity.setAuctionDate(auction.auctionDate());
    entity.setLiveStartedAt(auction.liveStartedAt());
    entity.setClosedAt(auction.closedAt());
    entity.setAnnouncementReference(auction.announcementReference());
    entity.setRealtimeChannel(buildRealtimeChannel(auction));
    entity.setCreatedAt(auction.createdAt());
    entity.setUpdatedAt(auction.updatedAt());

    List<AuctionLotEntity> lotEntities =
        auction.lots().stream().map(lot -> toLotEntity(lot, entity)).toList();
    entity.setLots(lotEntities);
    return entity;
  }

  private AuctionLotEntity toLotEntity(AuctionLot lot, AuctionEntity auction) {
    AuctionLotEntity entity = new AuctionLotEntity();
    entity.setId(lot.id());
    entity.setAuction(auction);
    entity.setLotNumber(lot.lotNumber());
    entity.setContractNumber(lot.contractNumber());
    entity.setItemNumber(lot.itemNumber());
    entity.setDescription(lot.description());
    entity.setEstimatedValue(lot.estimatedValue());
    entity.setOutstandingClaim(lot.outstandingClaim());
    entity.setLatestBidAmount(lot.latestBidAmount());
    entity.setLeadingBidder(lot.leadingBidder());
    entity.setHammerPrice(lot.hammerPrice());
    entity.setStatus(lot.status());
    entity.setSurplusAmount(lot.surplusAmount());
    entity.setAuthorityTransferDueDate(lot.authorityTransferDueDate());
    entity.setAuthorityTransferStatus(lot.authorityTransferStatus());
    return entity;
  }

  private Auction toDomain(AuctionEntity entity) {
    return new Auction(
        entity.getId(),
        entity.getTenantId(),
        entity.getTitle(),
        entity.getLocation(),
        entity.getStatus(),
        entity.getPublicAnnouncementDate(),
        entity.getAuctionDate(),
        entity.getLiveStartedAt(),
        entity.getClosedAt(),
        entity.getAnnouncementReference(),
        entity.getLots().stream().map(this::toLot).toList(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }

  private String buildRealtimeChannel(Auction auction) {
    return "auction." + auction.tenantId() + "." + auction.id();
  }

  private AuctionLot toLot(AuctionLotEntity entity) {
    return new AuctionLot(
        entity.getId(),
        entity.getAuction().getId(),
        entity.getLotNumber(),
        entity.getContractNumber(),
        entity.getItemNumber(),
        entity.getDescription(),
        entity.getEstimatedValue(),
        entity.getOutstandingClaim(),
        entity.getLatestBidAmount(),
        entity.getLeadingBidder(),
        entity.getHammerPrice(),
        entity.getStatus(),
        entity.getSurplusAmount(),
        entity.getAuthorityTransferDueDate(),
        entity.getAuthorityTransferStatus());
  }
}
