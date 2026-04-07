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
package io.lombardio.auction.api.http.mapper;

import io.lombardio.auction.api.http.AuctionLotResponse;
import io.lombardio.auction.api.http.AuctionResponse;
import io.lombardio.auction.api.http.CreateAuctionRequest;
import io.lombardio.auction.api.http.PlaceBidRequest;
import io.lombardio.auction.api.http.SurplusCaseResponse;
import io.lombardio.auction.application.service.AnnounceAuctionCommand;
import io.lombardio.auction.application.service.CreateAuctionCommand;
import io.lombardio.auction.application.service.PlaceBidCommand;
import io.lombardio.auction.application.service.SettleAuctionLotCommand;
import io.lombardio.auction.application.service.SurplusCase;
import io.lombardio.auction.domain.model.Auction;
import io.lombardio.auction.domain.model.AuctionLot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ApiMapper {

  AuctionResponse toResponse(Auction domain);

  AuctionLotResponse toLotResponse(AuctionLot domain);

  SurplusCaseResponse toSurplusResponse(SurplusCase domain);

  CreateAuctionCommand toCommand(CreateAuctionRequest request);

  PlaceBidCommand toCommand(PlaceBidRequest request);

  AnnounceAuctionCommand toCommand(
      io.lombardio.auction.api.http.AuctionStatusUpdateRequest request);

  @Mapping(target = "hammerPrice", source = "hammerPrice")
  SettleAuctionLotCommand toCommand(io.lombardio.auction.api.http.AuctionSettlementRequest request);
}
