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

import java.math.BigDecimal;

public record OnlineAuctionLot(
    String id,
    int lotNumber,
    String title,
    String description,
    BigDecimal startingBid,
    BigDecimal currentBid,
    String leadingPaddleNumber) {

  public OnlineAuctionLot applyBid(
      OnlineAuction auction, BidderRegistration bidder, BigDecimal amount) {
    BigDecimal minimumBid = currentBid.max(startingBid).add(auction.minimumIncrement());
    if (amount.compareTo(minimumBid) < 0) {
      throw new IllegalArgumentException("Bid must satisfy the minimum increment");
    }
    return new OnlineAuctionLot(
        id, lotNumber, title, description, startingBid, amount, bidder.paddleNumber());
  }
}
