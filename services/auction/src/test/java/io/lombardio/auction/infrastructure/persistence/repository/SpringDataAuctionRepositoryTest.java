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
package io.lombardio.auction.infrastructure.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import io.lombardio.auction.domain.model.AuctionLotStatus;
import io.lombardio.auction.domain.model.AuctionStatus;
import io.lombardio.auction.infrastructure.persistence.entity.AuctionEntity;
import io.lombardio.auction.infrastructure.persistence.entity.AuctionLotEntity;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SpringDataAuctionRepositoryTest {

  @Autowired private SpringDataAuctionRepository repository;

  @Autowired private EntityManager entityManager;

  @Test
  void loadsLotsWithAuctionListQuery() {
    AuctionEntity auction = new AuctionEntity();
    auction.setId("auction-1");
    auction.setTenantId("tenant-default");
    auction.setTitle("Fruehjahrsauktion");
    auction.setLocation("Berlin");
    auction.setStatus(AuctionStatus.DRAFT);
    auction.setRealtimeChannel("auction.tenant-default.auction-1");
    auction.setCreatedAt(Instant.parse("2026-03-18T12:00:00Z"));
    auction.setUpdatedAt(Instant.parse("2026-03-18T12:00:00Z"));

    AuctionLotEntity lot = new AuctionLotEntity();
    lot.setId("lot-1");
    lot.setAuction(auction);
    lot.setLotNumber(1);
    lot.setContractNumber("PS-5001");
    lot.setItemNumber("PS-5001-01");
    lot.setDescription("Goldring 585");
    lot.setEstimatedValue(new BigDecimal("500.00"));
    lot.setOutstandingClaim(new BigDecimal("300.00"));
    lot.setLatestBidAmount(BigDecimal.ZERO);
    lot.setStatus(AuctionLotStatus.PENDING);
    lot.setAuthorityTransferStatus("NOT_DUE");

    auction.setLots(List.of(lot));

    repository.saveAndFlush(auction);
    entityManager.clear();

    List<AuctionEntity> auctions = repository.findByTenantIdOrderByCreatedAtDesc("tenant-default");

    assertThat(auctions).hasSize(1);
    assertThat(Hibernate.isInitialized(auctions.get(0).getLots())).isTrue();
    assertThat(auctions.get(0).getLots()).hasSize(1);
  }
}
