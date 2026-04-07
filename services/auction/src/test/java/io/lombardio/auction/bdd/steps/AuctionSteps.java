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
package io.lombardio.auction.bdd.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.lombardio.auction.application.service.AnnounceAuctionCommand;
import io.lombardio.auction.application.service.AuctionService;
import io.lombardio.auction.application.service.CreateAuctionCommand;
import io.lombardio.auction.application.service.CreateAuctionLotCommand;
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

public class AuctionSteps {

  private AuctionService auctionService;
  private AuctionRepository auctionRepository;
  private Auction lastAuction;
  private List<CreateAuctionLotCommand> currentLots = new ArrayList<>();
  private final String tenantId = "tenant-bdd";

  @Before
  public void setup() {
    auctionRepository = new InMemoryAuctionRepository();
    Clock clock = Clock.fixed(Instant.parse("2026-04-08T10:00:00Z"), ZoneOffset.UTC);
    auctionService = new AuctionService(auctionRepository, clock);
    currentLots.clear();
  }

  @Given("the auction service is running")
  public void the_auction_service_is_running() {
    assertNotNull(auctionService);
  }

  @When("I create an auction with title {string} at {string}")
  public void i_create_an_auction_with_title_at(String title, String location) {
    // We store the title/location and wait for lots or create immediately
    CreateAuctionCommand command = new CreateAuctionCommand(title, location, currentLots);
    lastAuction = auctionService.createAuction(tenantId, command);
  }

  @When("I add a lot for contract {string} with estimated value {double}")
  public void i_add_a_lot_for_contract_with_estimated_value(String contractNumber, Double value) {
    // In our simplified BDD flow, we re-create the auction with the lot since the service expects
    // all lots at creation
    currentLots.add(
        new CreateAuctionLotCommand(
            contractNumber,
            "ITEM-1",
            "Description",
            BigDecimal.valueOf(value),
            BigDecimal.valueOf(value * 0.5)));

    CreateAuctionCommand command =
        new CreateAuctionCommand(lastAuction.title(), lastAuction.location(), currentLots);
    lastAuction = auctionService.createAuction(tenantId, command);
  }

  @Then("the auction should be created in status {string}")
  public void the_auction_should_be_created_in_status(String expectedStatus) {
    assertEquals(expectedStatus, lastAuction.status().name());
  }

  @Then("the auction should contain {int} lot")
  public void the_auction_should_contain_lot(Integer count) {
    assertEquals(count, lastAuction.lots().size());
  }

  @Given("an auction draft {string} exists")
  public void an_auction_draft_exists(String title) {
    i_create_an_auction_with_title_at(title, "Berlin");
  }

  @When("I announce the auction for {string} with reference {string}")
  public void i_announce_the_auction_for_with_reference(String date, String reference) {
    AnnounceAuctionCommand command = new AnnounceAuctionCommand(LocalDate.parse(date), reference);
    lastAuction = auctionService.announceAuction(tenantId, lastAuction.id(), command);
  }

  @When("I open the auction")
  public void i_open_the_auction() {
    lastAuction = auctionService.openAuction(tenantId, lastAuction.id());
  }

  @Then("the auction status should be {string}")
  public void the_auction_status_should_be(String expectedStatus) {
    assertEquals(expectedStatus, lastAuction.status().name());
  }

  private static final class InMemoryAuctionRepository implements AuctionRepository {
    private final List<Auction> auctions = new ArrayList<>();

    @Override
    public List<Auction> findByTenantId(String tenantId) {
      return auctions.stream().filter(a -> a.tenantId().equals(tenantId)).toList();
    }

    @Override
    public Optional<Auction> findByTenantIdAndId(String tenantId, String auctionId) {
      return auctions.stream()
          .filter(a -> a.tenantId().equals(tenantId) && a.id().equals(auctionId))
          .findFirst();
    }

    @Override
    public Auction save(Auction auction) {
      auctions.removeIf(a -> a.id().equals(auction.id()));
      auctions.add(auction);
      return auction;
    }
  }
}
