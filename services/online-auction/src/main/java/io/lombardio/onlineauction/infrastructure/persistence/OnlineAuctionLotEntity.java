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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "online_auction_lots", schema = "online_auction")
public class OnlineAuctionLotEntity {

  @Id private String id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "auction_id", nullable = false)
  private OnlineAuctionEntity auction;

  @Column(nullable = false)
  private int lotNumber;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false, length = 1000)
  private String description;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal startingBid;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal currentBid;

  private String highestBidderAlias;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public OnlineAuctionEntity getAuction() {
    return auction;
  }

  public void setAuction(OnlineAuctionEntity auction) {
    this.auction = auction;
  }

  public int getLotNumber() {
    return lotNumber;
  }

  public void setLotNumber(int lotNumber) {
    this.lotNumber = lotNumber;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public BigDecimal getStartingBid() {
    return startingBid;
  }

  public void setStartingBid(BigDecimal startingBid) {
    this.startingBid = startingBid;
  }

  public BigDecimal getCurrentBid() {
    return currentBid;
  }

  public void setCurrentBid(BigDecimal currentBid) {
    this.currentBid = currentBid;
  }

  public String getHighestBidderAlias() {
    return highestBidderAlias;
  }

  public void setHighestBidderAlias(String highestBidderAlias) {
    this.highestBidderAlias = highestBidderAlias;
  }
}
