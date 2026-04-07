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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "online_auctions")
@Getter
@Setter
@NoArgsConstructor
@SuppressFBWarnings(
    value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"},
    justification = "JPA entities")
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
}
