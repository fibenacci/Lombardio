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
package io.lombardio.auction.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.lombardio.auction.domain.model.Auction;
import io.lombardio.auction.domain.port.AuctionRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AuctionServiceTest {

  private final InMemoryAuctionRepository repository = new InMemoryAuctionRepository();
  private final AuctionService service =
      new AuctionService(
          repository, Clock.fixed(Instant.parse("2026-03-18T12:00:00Z"), ZoneOffset.UTC));

  @Test
  void createsAuctionAndCalculatesSurplusDueDate() {
    var response =
        service.createAuction(
            "tenant-default",
            new CreateAuctionCommand(
                "Fruehjahrsauktion",
                "Berlin",
                List.of(
                    new CreateAuctionLotCommand(
                        "PS-5001",
                        "PS-5001-01",
                        "Goldring 585",
                        new BigDecimal("500.00"),
                        new BigDecimal("300.00")))));

    assertThat(response.lots()).hasSize(1);

    var announced =
        service.announceAuction(
            "tenant-default",
            response.id(),
            new AnnounceAuctionCommand(LocalDate.of(2026, 3, 26), "Amtsblatt 12/2026"));
    var live = service.openAuction("tenant-default", announced.id());
    var afterBid =
        service.placeBid(
            "tenant-default",
            live.id(),
            live.lots().get(0).id(),
            new PlaceBidCommand("Bieter 17", new BigDecimal("620.00")));
    var settled =
        service.settleLot(
            "tenant-default",
            afterBid.id(),
            afterBid.lots().get(0).id(),
            new SettleAuctionLotCommand(new BigDecimal("620.00")));

    assertThat(settled.lots().get(0).surplusAmount()).isEqualByComparingTo("320.00");
    assertThat(settled.lots().get(0).authorityTransferDueDate())
        .isEqualTo(LocalDate.of(2029, 12, 31).plusMonths(1));
  }

  private static final class InMemoryAuctionRepository implements AuctionRepository {
    private final List<Auction> auctions = new ArrayList<>();

    @Override
    public List<Auction> findByTenantId(String tenantId) {
      return auctions.stream().filter(auction -> auction.tenantId().equals(tenantId)).toList();
    }

    @Override
    public Optional<Auction> findByTenantIdAndId(String tenantId, String auctionId) {
      return auctions.stream()
          .filter(auction -> auction.tenantId().equals(tenantId) && auction.id().equals(auctionId))
          .findFirst();
    }

    @Override
    public Auction save(Auction auction) {
      auctions.removeIf(existing -> existing.id().equals(auction.id()));
      auctions.add(auction);
      return auction;
    }
  }
}
