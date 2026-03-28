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
package io.lombardio.onlineauction.bootstrap;

import io.lombardio.onlineauction.demo.DemoDataProperties;
import io.lombardio.onlineauction.domain.BidderApprovalStatus;
import io.lombardio.onlineauction.domain.BidderRegistration;
import io.lombardio.onlineauction.domain.OnlineAuction;
import io.lombardio.onlineauction.domain.OnlineAuctionLot;
import io.lombardio.onlineauction.domain.OnlineAuctionRepository;
import io.lombardio.onlineauction.domain.OnlineAuctionStatus;
import io.lombardio.onlineauction.domain.ReviewCheckStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class OnlineAuctionDevelopmentSeeder {

  private record DemoTenant(String id, String key, String city) {}

  private static final List<DemoTenant> TENANTS =
      List.of(
          new DemoTenant("tenant-default", "default", "Berlin"),
          new DemoTenant("tenant-hamburg", "hanseatic", "Hamburg"),
          new DemoTenant("tenant-munich", "isar", "Muenchen"),
          new DemoTenant("tenant-cologne", "rhein", "Koeln"),
          new DemoTenant("tenant-stuttgart", "neckar", "Stuttgart"));

  private final OnlineAuctionRepository onlineAuctionRepository;
  private final DemoDataProperties demoDataProperties;

  OnlineAuctionDevelopmentSeeder(
      OnlineAuctionRepository onlineAuctionRepository, DemoDataProperties demoDataProperties) {
    this.onlineAuctionRepository = onlineAuctionRepository;
    this.demoDataProperties = demoDataProperties;
  }

  public void seed() {
    int tenantCount = tenantCount(demoDataProperties.effectiveScale());
    int auctionsPerTenant = auctionsPerTenant(demoDataProperties.effectiveScale());
    for (int tenantIndex = 0; tenantIndex < tenantCount; tenantIndex++) {
      DemoTenant tenant = TENANTS.get(tenantIndex);
      for (int auctionIndex = 1; auctionIndex <= auctionsPerTenant; auctionIndex++) {
        onlineAuctionRepository.save(buildAuction(tenant, tenantIndex, auctionIndex));
      }
    }
  }

  private OnlineAuction buildAuction(DemoTenant tenant, int tenantIndex, int auctionIndex) {
    OnlineAuctionStatus status =
        switch (auctionIndex % 4) {
          case 0 -> OnlineAuctionStatus.CLOSED;
          case 1 -> OnlineAuctionStatus.PUBLISHED;
          case 2 -> OnlineAuctionStatus.LIVE;
          default -> OnlineAuctionStatus.DRAFT;
        };

    Instant createdAt =
        Instant.now().minusSeconds((long) (auctionIndex + tenantIndex * 12) * 86_400L);
    Instant publishedAt =
        status == OnlineAuctionStatus.DRAFT ? null : createdAt.plusSeconds(43_200);
    Instant liveStartedAt =
        status == OnlineAuctionStatus.LIVE || status == OnlineAuctionStatus.CLOSED
            ? createdAt.plusSeconds(86_400)
            : null;
    Instant countdownEndsAt =
        status == OnlineAuctionStatus.LIVE
            ? Instant.now().plusSeconds(900 + auctionIndex * 60L)
            : null;
    Instant closedAt =
        status == OnlineAuctionStatus.CLOSED ? liveStartedAt.plusSeconds(21_600) : null;

    return new OnlineAuction(
        "online-auction-" + tenant.key() + "-" + String.format("%03d", auctionIndex),
        tenant.id(),
        tenant.city() + " Online Sale " + String.format("%02d", auctionIndex),
        tenant.key() + "-sale-" + String.format("%02d", auctionIndex),
        status,
        tenant.key() + ".auction.room." + auctionIndex,
        new BigDecimal("10.00"),
        120,
        publishedAt,
        liveStartedAt,
        countdownEndsAt,
        closedAt,
        createdAt,
        createdAt.plusSeconds(3_600),
        buildLots(tenant, auctionIndex, status),
        buildRegistrations(tenant, auctionIndex));
  }

  private List<OnlineAuctionLot> buildLots(
      DemoTenant tenant, int auctionIndex, OnlineAuctionStatus status) {
    List<OnlineAuctionLot> lots = new ArrayList<>();
    int lotCount = 5 + (auctionIndex % 3);
    for (int lotIndex = 1; lotIndex <= lotCount; lotIndex++) {
      BigDecimal startingBid = new BigDecimal(80 + lotIndex * 30L + ".00");
      BigDecimal currentBid =
          switch (status) {
            case DRAFT -> startingBid;
            case PUBLISHED -> startingBid.add(new BigDecimal("10.00"));
            case LIVE -> startingBid.add(new BigDecimal(45 + lotIndex * 8L + ".00"));
            case CLOSED -> startingBid.add(new BigDecimal(70 + lotIndex * 10L + ".00"));
          };

      lots.add(
          new OnlineAuctionLot(
              "online-lot-"
                  + tenant.key()
                  + "-"
                  + String.format("%03d", auctionIndex)
                  + "-"
                  + String.format("%02d", lotIndex),
              lotIndex,
              lotIndex % 2 == 0 ? "Designerschmuck Los " + lotIndex : "Elektronik Los " + lotIndex,
              lotIndex % 2 == 0
                  ? "Ring, Kette und Armband im Set"
                  : "Smartphone, Tablet oder Kamera im Bieterpaket",
              startingBid,
              currentBid,
              status == OnlineAuctionStatus.DRAFT
                  ? null
                  : "B" + String.format("%03d", 100 + lotIndex)));
    }
    return lots;
  }

  private List<BidderRegistration> buildRegistrations(DemoTenant tenant, int auctionIndex) {
    List<BidderRegistration> registrations = new ArrayList<>();
    int registrationCount = 4 + (auctionIndex % 4);
    for (int index = 1; index <= registrationCount; index++) {
      BidderApprovalStatus approvalStatus =
          switch (index % 5) {
            case 0 -> BidderApprovalStatus.REJECTED;
            case 1, 3 -> BidderApprovalStatus.APPROVED;
            default -> BidderApprovalStatus.PENDING;
          };
      ReviewCheckStatus reviewStatus =
          approvalStatus == BidderApprovalStatus.REJECTED
              ? ReviewCheckStatus.FAILED
              : approvalStatus == BidderApprovalStatus.APPROVED
                  ? ReviewCheckStatus.PASSED
                  : ReviewCheckStatus.PENDING;

      registrations.add(
          new BidderRegistration(
              "bidder-"
                  + tenant.key()
                  + "-"
                  + String.format("%03d", auctionIndex)
                  + "-"
                  + String.format("%02d", index),
              "Bieter " + index,
              "bidder" + index + "." + tenant.key() + "@demo.lombardio.local",
              "Demo Bieter " + index,
              LocalDate.of(1970 + index, ((index - 1) % 12) + 1, ((index - 1) % 27) + 1).toString(),
              "DE1250010517064848" + String.format("%02d", index),
              tenant.key().substring(0, Math.min(3, tenant.key().length())).toUpperCase()
                  + String.format("%03d", auctionIndex * 10 + index),
              "token-" + tenant.key() + "-" + auctionIndex + "-" + index,
              approvalStatus,
              reviewStatus,
              approvalStatus == BidderApprovalStatus.REJECTED
                  ? ReviewCheckStatus.FAILED
                  : ReviewCheckStatus.PASSED,
              approvalStatus == BidderApprovalStatus.REJECTED
                  ? "Adressnachweis unvollstaendig"
                  : null,
              approvalStatus == BidderApprovalStatus.APPROVED
                  ? Instant.now().minusSeconds(index * 1_800L)
                  : null,
              Instant.now().minusSeconds((long) (auctionIndex * 10 + index) * 3_600L)));
    }
    return registrations;
  }

  private int tenantCount(String scale) {
    return switch (normalize(scale)) {
      case "small" -> 2;
      case "large" -> TENANTS.size();
      default -> 4;
    };
  }

  private int auctionsPerTenant(String scale) {
    return switch (normalize(scale)) {
      case "small" -> 2;
      case "large" -> 5;
      default -> 3;
    };
  }

  private String normalize(String scale) {
    return scale == null ? "medium" : scale.trim().toLowerCase();
  }
}
