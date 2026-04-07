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
package io.lombardio.onlineauction.infrastructure.persistence;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.lombardio.onlineauction.domain.BidderApprovalStatus;
import io.lombardio.onlineauction.domain.ReviewCheckStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bidder_registrations")
@Getter
@Setter
@NoArgsConstructor
@SuppressFBWarnings(
    value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"},
    justification = "JPA entities")
public class BidderRegistrationEntity {

  @Id private String id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "auction_id", nullable = false)
  private OnlineAuctionEntity auction;

  @Column(nullable = false)
  private String displayName;

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private String legalName;

  @Column(nullable = false)
  private String birthDate;

  @Column(nullable = false)
  private String iban;

  @Column(nullable = false)
  private String paddleNumber;

  @Column(nullable = false)
  private String accessToken;

  @Column private String accessTokenHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private BidderApprovalStatus approvalStatus;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ReviewCheckStatus kycStatus;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ReviewCheckStatus accountCheckStatus;

  @Column(length = 1000)
  private String reviewNote;

  private Instant approvedAt;

  @Column(nullable = false)
  private Instant createdAt;
}
