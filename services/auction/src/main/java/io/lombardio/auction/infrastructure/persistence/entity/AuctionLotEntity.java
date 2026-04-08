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
package io.lombardio.auction.infrastructure.persistence.entity;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.lombardio.auction.domain.model.AuctionLotStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "auction_lots")
@Getter
@Setter
@NoArgsConstructor
@SuppressFBWarnings(
    value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"},
    justification = "JPA entities")
public class AuctionLotEntity {

  @Id private String id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "auction_id", nullable = false)
  private AuctionEntity auction;

  @Column(nullable = false)
  private int lotNumber;

  @Column(nullable = false)
  private String contractNumber;

  @Column(nullable = false)
  private String itemNumber;

  @Column(nullable = false, length = 1000)
  private String description;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal estimatedValue;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal outstandingClaim;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal latestBidAmount;

  private String leadingBidder;

  @Column(precision = 19, scale = 2)
  private BigDecimal hammerPrice;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AuctionLotStatus status;

  @Column(precision = 19, scale = 2)
  private BigDecimal surplusAmount;

  private LocalDate authorityTransferDueDate;

  private String authorityTransferStatus;
}
