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
package io.lombardio.onlineauction.infrastructure.persistence.mapper;

import io.lombardio.onlineauction.domain.BidderRegistration;
import io.lombardio.onlineauction.domain.OnlineAuction;
import io.lombardio.onlineauction.domain.OnlineAuctionLot;
import io.lombardio.onlineauction.infrastructure.persistence.BidderRegistrationEntity;
import io.lombardio.onlineauction.infrastructure.persistence.OnlineAuctionEntity;
import io.lombardio.onlineauction.infrastructure.persistence.OnlineAuctionLotEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PersistenceMapper {

  @Mapping(target = "lots", ignore = true)
  @Mapping(target = "registrations", ignore = true)
  OnlineAuctionEntity toEntity(OnlineAuction domain);

  OnlineAuction toDomain(OnlineAuctionEntity entity);

  @Mapping(target = "auction", ignore = true)
  @Mapping(target = "highestBidderAlias", source = "leadingPaddleNumber")
  OnlineAuctionLotEntity toLotEntity(OnlineAuctionLot domain);

  @Mapping(target = "leadingPaddleNumber", source = "highestBidderAlias")
  OnlineAuctionLot toLotDomain(OnlineAuctionLotEntity entity);

  @Mapping(target = "auction", ignore = true)
  @Mapping(target = "accessToken", source = "id")
  @Mapping(target = "withAccessToken", ignore = true)
  BidderRegistrationEntity toRegistrationEntity(BidderRegistration domain);

  @Mapping(
      target = "accessTokenHash",
      expression =
          "java(entity.getAccessTokenHash() != null ? entity.getAccessTokenHash() : io.lombardio.onlineauction.application.BidderAccessTokenHasher.sha256(entity.getAccessToken()))")
  @Mapping(target = "accessToken", ignore = true)
  BidderRegistration toRegistrationDomain(BidderRegistrationEntity entity);

  @AfterMapping
  default void linkAssociations(@MappingTarget OnlineAuctionEntity entity, OnlineAuction domain) {
    if (domain.lots() != null) {
      for (OnlineAuctionLot lot : domain.lots()) {
        OnlineAuctionLotEntity lotEntity = toLotEntity(lot);
        lotEntity.setAuction(entity);
        entity.getLots().add(lotEntity);
      }
    }
    if (domain.registrations() != null) {
      for (BidderRegistration reg : domain.registrations()) {
        BidderRegistrationEntity regEntity = toRegistrationEntity(reg);
        regEntity.setAuction(entity);
        entity.getRegistrations().add(regEntity);
      }
    }
  }
}
