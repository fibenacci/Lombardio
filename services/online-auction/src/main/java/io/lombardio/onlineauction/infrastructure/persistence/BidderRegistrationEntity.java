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

@Entity
@Table(name = "bidder_registrations")
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

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getLegalName() {
    return legalName;
  }

  public void setLegalName(String legalName) {
    this.legalName = legalName;
  }

  public String getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(String birthDate) {
    this.birthDate = birthDate;
  }

  public String getIban() {
    return iban;
  }

  public void setIban(String iban) {
    this.iban = iban;
  }

  public String getPaddleNumber() {
    return paddleNumber;
  }

  public void setPaddleNumber(String paddleNumber) {
    this.paddleNumber = paddleNumber;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public String getAccessTokenHash() {
    return accessTokenHash;
  }

  public void setAccessTokenHash(String accessTokenHash) {
    this.accessTokenHash = accessTokenHash;
  }

  public BidderApprovalStatus getApprovalStatus() {
    return approvalStatus;
  }

  public void setApprovalStatus(BidderApprovalStatus approvalStatus) {
    this.approvalStatus = approvalStatus;
  }

  public ReviewCheckStatus getKycStatus() {
    return kycStatus;
  }

  public void setKycStatus(ReviewCheckStatus kycStatus) {
    this.kycStatus = kycStatus;
  }

  public ReviewCheckStatus getAccountCheckStatus() {
    return accountCheckStatus;
  }

  public void setAccountCheckStatus(ReviewCheckStatus accountCheckStatus) {
    this.accountCheckStatus = accountCheckStatus;
  }

  public String getReviewNote() {
    return reviewNote;
  }

  public void setReviewNote(String reviewNote) {
    this.reviewNote = reviewNote;
  }

  public Instant getApprovedAt() {
    return approvedAt;
  }

  public void setApprovedAt(Instant approvedAt) {
    this.approvedAt = approvedAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
}
