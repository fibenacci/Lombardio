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
import io.lombardio.auction.domain.model.AuctionStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "auctions")
@Getter
@Setter
@NoArgsConstructor
@SuppressFBWarnings(
    value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"},
    justification = "JPA entities and collections")
public class AuctionEntity {

  @Id private String id;

  @Column(nullable = false)
  private String tenantId;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String location;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AuctionStatus status;

  private LocalDate publicAnnouncementDate;

  private LocalDate auctionDate;

  private Instant liveStartedAt;

  private Instant closedAt;

  private String announcementReference;

  @Column(nullable = false)
  private String realtimeChannel;

  @OneToMany(mappedBy = "auction", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("lotNumber asc")
  private List<AuctionLotEntity> lots = new ArrayList<>();

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;
}
