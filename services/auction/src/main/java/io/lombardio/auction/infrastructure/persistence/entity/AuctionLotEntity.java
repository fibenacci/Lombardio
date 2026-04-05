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
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "auction_lots")
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

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP",
      justification = "JPA relationship references must expose the managed entity association")
  public AuctionEntity getAuction() {
    return auction;
  }

  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP2",
      justification =
          "JPA relationship references must store the managed entity association directly")
  public void setAuction(@NotNull AuctionEntity auction) {
    this.auction = Objects.requireNonNull(auction, "auction");
  }

  public int getLotNumber() {
    return lotNumber;
  }

  public void setLotNumber(int lotNumber) {
    this.lotNumber = lotNumber;
  }

  public String getContractNumber() {
    return contractNumber;
  }

  public void setContractNumber(@NotNull String contractNumber) {
    this.contractNumber = Objects.requireNonNull(contractNumber, "contractNumber");
  }

  public String getItemNumber() {
    return itemNumber;
  }

  public void setItemNumber(@NotNull String itemNumber) {
    this.itemNumber = Objects.requireNonNull(itemNumber, "itemNumber");
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(@NotNull String description) {
    this.description = Objects.requireNonNull(description, "description");
  }

  public BigDecimal getEstimatedValue() {
    return estimatedValue;
  }

  public void setEstimatedValue(@NotNull BigDecimal estimatedValue) {
    this.estimatedValue = Objects.requireNonNull(estimatedValue, "estimatedValue");
  }

  public BigDecimal getOutstandingClaim() {
    return outstandingClaim;
  }

  public void setOutstandingClaim(@NotNull BigDecimal outstandingClaim) {
    this.outstandingClaim = Objects.requireNonNull(outstandingClaim, "outstandingClaim");
  }

  public BigDecimal getLatestBidAmount() {
    return latestBidAmount;
  }

  public void setLatestBidAmount(@NotNull BigDecimal latestBidAmount) {
    this.latestBidAmount = Objects.requireNonNull(latestBidAmount, "latestBidAmount");
  }

  public String getLeadingBidder() {
    return leadingBidder;
  }

  public void setLeadingBidder(String leadingBidder) {
    this.leadingBidder = leadingBidder;
  }

  public BigDecimal getHammerPrice() {
    return hammerPrice;
  }

  public void setHammerPrice(BigDecimal hammerPrice) {
    this.hammerPrice = hammerPrice;
  }

  public AuctionLotStatus getStatus() {
    return status;
  }

  public void setStatus(@NotNull AuctionLotStatus status) {
    this.status = Objects.requireNonNull(status, "status");
  }

  public BigDecimal getSurplusAmount() {
    return surplusAmount;
  }

  public void setSurplusAmount(BigDecimal surplusAmount) {
    this.surplusAmount = surplusAmount;
  }

  public LocalDate getAuthorityTransferDueDate() {
    return authorityTransferDueDate;
  }

  public void setAuthorityTransferDueDate(LocalDate authorityTransferDueDate) {
    this.authorityTransferDueDate = authorityTransferDueDate;
  }

  public String getAuthorityTransferStatus() {
    return authorityTransferStatus;
  }

  public void setAuthorityTransferStatus(String authorityTransferStatus) {
    this.authorityTransferStatus = authorityTransferStatus;
  }
}
