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
package io.lombardio.onlineauction.domain;

import io.lombardio.onlineauction.api.RegisterBidderRequest;
import java.time.Instant;
import java.util.UUID;

public record BidderRegistration(
    String id,
    String displayName,
    String email,
    String legalName,
    String birthDate,
    String iban,
    String paddleNumber,
    String accessToken,
    String accessTokenHash,
    BidderApprovalStatus approvalStatus,
    ReviewCheckStatus kycStatus,
    ReviewCheckStatus accountCheckStatus,
    String reviewNote,
    Instant approvedAt,
    Instant createdAt) {

  public static BidderRegistration create(
      OnlineAuction auction,
      RegisterBidderRequest request,
      String rawAccessToken,
      String hashedAccessToken,
      Instant now) {
    return new BidderRegistration(
        "obr-" + UUID.randomUUID(),
        request.displayName(),
        request.email(),
        request.legalName(),
        request.birthDate(),
        request.iban(),
        "P" + (1000 + auction.registrations().size() + 1),
        null,
        hashedAccessToken,
        BidderApprovalStatus.PENDING,
        ReviewCheckStatus.PENDING,
        ReviewCheckStatus.PENDING,
        null,
        null,
        now);
  }

  public BidderRegistration withAccessToken(String rawAccessToken) {
    return new BidderRegistration(
        id,
        displayName,
        email,
        legalName,
        birthDate,
        iban,
        paddleNumber,
        rawAccessToken,
        accessTokenHash,
        approvalStatus,
        kycStatus,
        accountCheckStatus,
        reviewNote,
        approvedAt,
        createdAt);
  }

  public BidderRegistration review(
      BidderApprovalStatus approvalStatus,
      ReviewCheckStatus kycStatus,
      ReviewCheckStatus accountCheckStatus,
      String reviewNote,
      Instant now) {
    Instant approvedAt = (approvalStatus == BidderApprovalStatus.APPROVED) ? now : this.approvedAt;
    return new BidderRegistration(
        id,
        displayName,
        email,
        legalName,
        birthDate,
        iban,
        paddleNumber,
        accessToken,
        accessTokenHash,
        approvalStatus,
        kycStatus,
        accountCheckStatus,
        reviewNote,
        approvedAt,
        createdAt);
  }
}
