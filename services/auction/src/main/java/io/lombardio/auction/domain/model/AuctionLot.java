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
package io.lombardio.auction.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

public record AuctionLot(
    String id,
    String auctionId,
    int lotNumber,
    String contractNumber,
    String itemNumber,
    String description,
    BigDecimal estimatedValue,
    BigDecimal outstandingClaim,
    BigDecimal latestBidAmount,
    String leadingBidder,
    BigDecimal hammerPrice,
    AuctionLotStatus status,
    BigDecimal surplusAmount,
    LocalDate authorityTransferDueDate,
    String authorityTransferStatus) {

  public AuctionLot open() {
    return new AuctionLot(
        id,
        auctionId,
        lotNumber,
        contractNumber,
        itemNumber,
        description,
        estimatedValue,
        outstandingClaim,
        latestBidAmount,
        leadingBidder,
        hammerPrice,
        AuctionLotStatus.OPEN,
        surplusAmount,
        authorityTransferDueDate,
        authorityTransferStatus);
  }

  public AuctionLot placeBid(String bidder, BigDecimal amount) {
    BigDecimal current = latestBidAmount == null ? BigDecimal.ZERO : latestBidAmount;
    if (amount.compareTo(current) <= 0) {
      throw new IllegalArgumentException("Bid amount must be higher than the current bid");
    }
    return new AuctionLot(
        id,
        auctionId,
        lotNumber,
        contractNumber,
        itemNumber,
        description,
        estimatedValue,
        outstandingClaim,
        amount,
        bidder,
        hammerPrice,
        AuctionLotStatus.OPEN,
        surplusAmount,
        authorityTransferDueDate,
        authorityTransferStatus);
  }

  public AuctionLot settle(BigDecimal hammerPrice, LocalDate settlementDate) {
    BigDecimal surplus = hammerPrice.subtract(outstandingClaim).max(BigDecimal.ZERO);
    LocalDate transferDueDate =
        surplus.compareTo(BigDecimal.ZERO) > 0
            ? LocalDate.of(settlementDate.getYear(), Month.DECEMBER, 31).plusYears(3).plusMonths(1)
            : null;

    return new AuctionLot(
        id,
        auctionId,
        lotNumber,
        contractNumber,
        itemNumber,
        description,
        estimatedValue,
        outstandingClaim,
        latestBidAmount,
        leadingBidder,
        hammerPrice,
        hammerPrice.compareTo(BigDecimal.ZERO) > 0
            ? AuctionLotStatus.SOLD
            : AuctionLotStatus.UNSOLD,
        surplus,
        transferDueDate,
        surplus.compareTo(BigDecimal.ZERO) > 0 ? "OPEN" : "NOT_APPLICABLE");
  }
}
