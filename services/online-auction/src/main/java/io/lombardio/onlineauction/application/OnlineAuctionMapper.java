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

import io.lombardio.onlineauction.api.BidderRegistrationResponse;
import io.lombardio.onlineauction.api.OnlineAuctionLotResponse;
import io.lombardio.onlineauction.api.OnlineAuctionResponse;
import io.lombardio.onlineauction.domain.BidderRegistration;
import io.lombardio.onlineauction.domain.OnlineAuction;
import io.lombardio.onlineauction.domain.OnlineAuctionLot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public abstract class OnlineAuctionMapper {

  @Mapping(
      target = "registrations",
      source = "registrations",
      qualifiedByName = "mapPublicRegistration")
  @Mapping(target = "withRegistrations", ignore = true)
  public abstract OnlineAuctionResponse toAdminResponse(OnlineAuction auction);

  @Mapping(target = "registrations", expression = "java(java.util.List.of())")
  @Mapping(target = "withRegistrations", ignore = true)
  public abstract OnlineAuctionResponse toPublicResponse(OnlineAuction auction);

  public BidderRegistrationResponse toRegistrationResponse(
      BidderRegistration registration, boolean includeAccessToken) {
    if (registration == null) {
      return null;
    }
    return new BidderRegistrationResponse(
        registration.id(),
        registration.displayName(),
        registration.email(),
        registration.legalName(),
        registration.birthDate(),
        maskIban(registration.iban()),
        registration.paddleNumber(),
        includeAccessToken ? registration.accessToken() : null,
        registration.approvalStatus(),
        registration.kycStatus(),
        registration.accountCheckStatus(),
        registration.reviewNote(),
        registration.approvedAt(),
        registration.createdAt());
  }

  @Named("mapPublicRegistration")
  protected BidderRegistrationResponse mapPublicRegistration(BidderRegistration registration) {
    return toRegistrationResponse(registration, false);
  }

  @Mapping(target = "highestBidderAlias", source = "leadingPaddleNumber")
  protected abstract OnlineAuctionLotResponse toLotResponse(OnlineAuctionLot lot);

  protected String maskIban(String iban) {
    if (iban == null || iban.length() < 4) {
      return "****";
    }
    return "****" + iban.substring(Math.max(0, iban.length() - 4));
  }
}
