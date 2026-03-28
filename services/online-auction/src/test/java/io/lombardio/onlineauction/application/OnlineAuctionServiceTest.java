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
package io.lombardio.onlineauction.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.lombardio.onlineauction.api.CreateOnlineAuctionRequest;
import io.lombardio.onlineauction.api.OnlineAuctionLotRequest;
import io.lombardio.onlineauction.api.PlaceOnlineBidRequest;
import io.lombardio.onlineauction.api.RegisterBidderRequest;
import io.lombardio.onlineauction.domain.OnlineAuction;
import io.lombardio.onlineauction.domain.OnlineAuctionRepository;
import io.lombardio.onlineauction.domain.RealtimeSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class OnlineAuctionServiceTest {

  @Test
  void createsPublishesRegistersAndPlacesBid() {
    InMemoryOnlineAuctionRepository repository = new InMemoryOnlineAuctionRepository();
    OnlineAuctionService service =
        new OnlineAuctionService(
            repository,
            (channel, payload) -> {},
            (subject, channel) -> new RealtimeSession("ws://localhost", channel, "conn", "sub"));

    var created =
        service.createAuction(
            "tenant-default",
            new CreateOnlineAuctionRequest(
                "Live Gold Auction",
                "gold-live",
                new BigDecimal("10.00"),
                180,
                List.of(
                    new OnlineAuctionLotRequest(
                        "Goldring", "585 Goldring", new BigDecimal("100.00")))));
    assertEquals("DRAFT", created.status().name());

    var published = service.publishAuction("tenant-default", created.id());
    assertEquals("PUBLISHED", published.status().name());

    var live = service.startAuction("tenant-default", created.id());
    assertEquals("LIVE", live.status().name());

    var registration =
        service.registerBidder(
            "tenant-default",
            created.id(),
            new RegisterBidderRequest(
                "Anna",
                "anna@example.com",
                "Anna Beispiel",
                "1990-02-14",
                "DE44500105175407324931"));
    assertFalse(registration.accessToken().isBlank());
    assertEquals("PENDING", registration.approvalStatus().name());

    var reviewed =
        service.reviewRegistration(
            "tenant-default",
            created.id(),
            registration.id(),
            new io.lombardio.onlineauction.api.BidderReviewRequest(
                "PASSED", "PASSED", "APPROVE", "KYC and payout account checked"));
    assertEquals("APPROVED", reviewed.registrations().get(0).approvalStatus().name());

    var firstLot = live.lots().get(0);
    var bidResult =
        service.placeBid(
            "tenant-default",
            created.id(),
            new PlaceOnlineBidRequest(
                registration.accessToken(), firstLot.id(), new BigDecimal("110.00")));
    assertEquals(new BigDecimal("110.00"), bidResult.lots().get(0).currentBid());
    assertEquals(registration.paddleNumber(), bidResult.lots().get(0).highestBidderAlias());
  }

  private static final class InMemoryOnlineAuctionRepository implements OnlineAuctionRepository {
    private final List<OnlineAuction> auctions = new ArrayList<>();

    @Override
    public List<OnlineAuction> findByTenantId(String tenantId) {
      return auctions.stream().filter(item -> item.tenantId().equals(tenantId)).toList();
    }

    @Override
    public List<OnlineAuction> findPublicByTenantId(String tenantId) {
      return findByTenantId(tenantId).stream()
          .filter(item -> item.status().name().matches("PUBLISHED|LIVE|CLOSED"))
          .toList();
    }

    @Override
    public Optional<OnlineAuction> findByTenantIdAndId(String tenantId, String auctionId) {
      return auctions.stream()
          .filter(item -> item.tenantId().equals(tenantId) && item.id().equals(auctionId))
          .findFirst();
    }

    @Override
    public Optional<OnlineAuction> findPublicByTenantIdAndId(String tenantId, String auctionId) {
      return findByTenantIdAndId(tenantId, auctionId)
          .filter(item -> item.status().name().matches("PUBLISHED|LIVE|CLOSED"));
    }

    @Override
    public OnlineAuction save(OnlineAuction auction) {
      auctions.removeIf(item -> item.id().equals(auction.id()));
      auctions.add(auction);
      return auction;
    }
  }
}
