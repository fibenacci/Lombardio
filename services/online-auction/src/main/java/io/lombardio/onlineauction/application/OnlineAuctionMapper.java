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
import java.util.List;

final class OnlineAuctionMapper {

  OnlineAuctionResponse toAdminResponse(OnlineAuction auction) {
    return new OnlineAuctionResponse(
        auction.id(),
        auction.tenantId(),
        auction.title(),
        auction.slug(),
        auction.status(),
        auction.channelName(),
        auction.minimumIncrement(),
        auction.countdownSeconds(),
        auction.publishedAt(),
        auction.liveStartedAt(),
        auction.countdownEndsAt(),
        auction.closedAt(),
        auction.lots().stream().map(this::toLotResponse).toList(),
        auction.registrations().stream()
            .map(registration -> toRegistrationResponse(registration, false))
            .toList());
  }

  OnlineAuctionResponse toPublicResponse(OnlineAuction auction) {
    return new OnlineAuctionResponse(
        auction.id(),
        auction.tenantId(),
        auction.title(),
        auction.slug(),
        auction.status(),
        auction.channelName(),
        auction.minimumIncrement(),
        auction.countdownSeconds(),
        auction.publishedAt(),
        auction.liveStartedAt(),
        auction.countdownEndsAt(),
        auction.closedAt(),
        auction.lots().stream().map(this::toLotResponse).toList(),
        List.of());
  }

  BidderRegistrationResponse toRegistrationResponse(
      BidderRegistration registration, boolean includeAccessToken) {
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

  private OnlineAuctionLotResponse toLotResponse(OnlineAuctionLot lot) {
    return new OnlineAuctionLotResponse(
        lot.id(),
        lot.lotNumber(),
        lot.title(),
        lot.description(),
        lot.startingBid(),
        lot.currentBid(),
        lot.highestBidderAlias());
  }

  private String maskIban(String iban) {
    if (iban == null || iban.length() < 4) {
      return "****";
    }
    return "****" + iban.substring(Math.max(0, iban.length() - 4));
  }
}
