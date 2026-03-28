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

import io.lombardio.onlineauction.domain.OnlineAuctionStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "online_auctions", schema = "online_auction")
public class OnlineAuctionEntity {

  @Id private String id;

  @Column(nullable = false)
  private String tenantId;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String slug;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OnlineAuctionStatus status;

  @Column(nullable = false)
  private String channelName;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal minimumIncrement;

  @Column(nullable = false)
  private int countdownSeconds;

  private Instant publishedAt;
  private Instant liveStartedAt;
  private Instant countdownEndsAt;
  private Instant closedAt;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  @OneToMany(mappedBy = "auction", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("lotNumber asc")
  private List<OnlineAuctionLotEntity> lots = new ArrayList<>();

  @OneToMany(mappedBy = "auction", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("createdAt asc")
  private List<BidderRegistrationEntity> registrations = new ArrayList<>();

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getSlug() {
    return slug;
  }

  public void setSlug(String slug) {
    this.slug = slug;
  }

  public OnlineAuctionStatus getStatus() {
    return status;
  }

  public void setStatus(OnlineAuctionStatus status) {
    this.status = status;
  }

  public String getChannelName() {
    return channelName;
  }

  public void setChannelName(String channelName) {
    this.channelName = channelName;
  }

  public BigDecimal getMinimumIncrement() {
    return minimumIncrement;
  }

  public void setMinimumIncrement(BigDecimal minimumIncrement) {
    this.minimumIncrement = minimumIncrement;
  }

  public int getCountdownSeconds() {
    return countdownSeconds;
  }

  public void setCountdownSeconds(int countdownSeconds) {
    this.countdownSeconds = countdownSeconds;
  }

  public Instant getPublishedAt() {
    return publishedAt;
  }

  public void setPublishedAt(Instant publishedAt) {
    this.publishedAt = publishedAt;
  }

  public Instant getLiveStartedAt() {
    return liveStartedAt;
  }

  public void setLiveStartedAt(Instant liveStartedAt) {
    this.liveStartedAt = liveStartedAt;
  }

  public Instant getCountdownEndsAt() {
    return countdownEndsAt;
  }

  public void setCountdownEndsAt(Instant countdownEndsAt) {
    this.countdownEndsAt = countdownEndsAt;
  }

  public Instant getClosedAt() {
    return closedAt;
  }

  public void setClosedAt(Instant closedAt) {
    this.closedAt = closedAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }

  public List<OnlineAuctionLotEntity> getLots() {
    return lots;
  }

  public void setLots(List<OnlineAuctionLotEntity> lots) {
    this.lots = lots;
  }

  public List<BidderRegistrationEntity> getRegistrations() {
    return registrations;
  }

  public void setRegistrations(List<BidderRegistrationEntity> registrations) {
    this.registrations = registrations;
  }
}
