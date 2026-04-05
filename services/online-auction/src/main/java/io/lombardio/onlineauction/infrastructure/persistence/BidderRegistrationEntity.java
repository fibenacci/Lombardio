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
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Objects;

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

  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP",
      justification = "JPA relationship references must expose the managed entity association")
  public OnlineAuctionEntity getAuction() {
    return auction;
  }

  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP2",
      justification =
          "JPA relationship references must store the managed entity association directly")
  public void setAuction(@NotNull OnlineAuctionEntity auction) {
    this.auction = Objects.requireNonNull(auction, "auction");
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(@NotNull String displayName) {
    this.displayName = Objects.requireNonNull(displayName, "displayName");
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(@NotNull String email) {
    this.email = Objects.requireNonNull(email, "email");
  }

  public String getLegalName() {
    return legalName;
  }

  public void setLegalName(@NotNull String legalName) {
    this.legalName = Objects.requireNonNull(legalName, "legalName");
  }

  public String getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(@NotNull String birthDate) {
    this.birthDate = Objects.requireNonNull(birthDate, "birthDate");
  }

  public String getIban() {
    return iban;
  }

  public void setIban(@NotNull String iban) {
    this.iban = Objects.requireNonNull(iban, "iban");
  }

  public String getPaddleNumber() {
    return paddleNumber;
  }

  public void setPaddleNumber(@NotNull String paddleNumber) {
    this.paddleNumber = Objects.requireNonNull(paddleNumber, "paddleNumber");
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(@NotNull String accessToken) {
    this.accessToken = Objects.requireNonNull(accessToken, "accessToken");
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

  public void setApprovalStatus(@NotNull BidderApprovalStatus approvalStatus) {
    this.approvalStatus = Objects.requireNonNull(approvalStatus, "approvalStatus");
  }

  public ReviewCheckStatus getKycStatus() {
    return kycStatus;
  }

  public void setKycStatus(@NotNull ReviewCheckStatus kycStatus) {
    this.kycStatus = Objects.requireNonNull(kycStatus, "kycStatus");
  }

  public ReviewCheckStatus getAccountCheckStatus() {
    return accountCheckStatus;
  }

  public void setAccountCheckStatus(@NotNull ReviewCheckStatus accountCheckStatus) {
    this.accountCheckStatus = Objects.requireNonNull(accountCheckStatus, "accountCheckStatus");
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

  public void setCreatedAt(@NotNull Instant createdAt) {
    this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
  }
}
