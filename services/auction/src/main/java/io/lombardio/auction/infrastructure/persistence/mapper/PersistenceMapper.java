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
package io.lombardio.auction.infrastructure.persistence.mapper;

import io.lombardio.auction.domain.model.Auction;
import io.lombardio.auction.domain.model.AuctionLot;
import io.lombardio.auction.infrastructure.persistence.entity.AuctionEntity;
import io.lombardio.auction.infrastructure.persistence.entity.AuctionLotEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PersistenceMapper {

  @Mapping(target = "lots", ignore = true)
  @Mapping(target = "realtimeChannel", ignore = true)
  AuctionEntity toEntity(Auction domain);

  Auction toDomain(AuctionEntity entity);

  @Mapping(target = "auction", ignore = true)
  AuctionLotEntity toLotEntity(AuctionLot domain);

  @Mapping(target = "auctionId", source = "auction.id")
  AuctionLot toLotDomain(AuctionLotEntity entity);

  @AfterMapping
  default void afterToEntity(@MappingTarget AuctionEntity entity, Auction domain) {
    if (domain.lots() != null) {
      for (AuctionLot lot : domain.lots()) {
        AuctionLotEntity lotEntity = toLotEntity(lot);
        lotEntity.setAuction(entity);
        entity.getLots().add(lotEntity);
      }
    }
    entity.setRealtimeChannel("auction." + domain.tenantId() + "." + domain.id());
  }
}
