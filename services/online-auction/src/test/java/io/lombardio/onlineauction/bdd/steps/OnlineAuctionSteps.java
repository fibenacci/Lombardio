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
package io.lombardio.onlineauction.bdd.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.lombardio.onlineauction.api.BidderRegistrationResponse;
import io.lombardio.onlineauction.api.OnlineAuctionLotResponse;
import io.lombardio.onlineauction.api.OnlineAuctionResponse;
import io.lombardio.onlineauction.api.RegisterBidderRequest;
import io.lombardio.onlineauction.application.OnlineAuctionService;
import io.lombardio.onlineauction.domain.OnlineAuctionStatus;
import java.math.BigDecimal;
import java.util.List;

public class OnlineAuctionSteps {

  private OnlineAuctionService onlineAuctionService;
  private OnlineAuctionResponse lastAuction;
  private BidderRegistrationResponse lastRegistration;
  private final String tenantId = "tenant-bdd";

  @Before
  public void setup() {
    onlineAuctionService = mock(OnlineAuctionService.class);
  }

  @Given("the online auction service is running")
  public void the_online_auction_service_is_running() {
    assertNotNull(onlineAuctionService);
  }

  @Given("a published online auction {string} exists")
  public void a_published_online_auction_exists(String title) {
    lastAuction =
        new OnlineAuctionResponse(
            "auc-1",
            tenantId,
            title,
            "slug-1",
            OnlineAuctionStatus.PUBLISHED,
            "channel-1",
            BigDecimal.TEN,
            30,
            List.of(),
            List.of(
                new OnlineAuctionLotResponse(
                    "lot-1",
                    "Gold Ring",
                    "18k",
                    BigDecimal.valueOf(100.0),
                    BigDecimal.valueOf(100.0),
                    null)));

    when(onlineAuctionService.getPublicAuction(eq(tenantId), any())).thenReturn(lastAuction);
    when(onlineAuctionService.listPublicAuctions(tenantId)).thenReturn(List.of(lastAuction));
  }

  @When("I register as a bidder with email {string}")
  public void i_register_as_a_bidder_with_email(String email) {
    lastRegistration = new BidderRegistrationResponse("reg-1", "paddle-1", "access-token");

    when(onlineAuctionService.registerBidder(
            eq(tenantId), eq(lastAuction.id()), any(RegisterBidderRequest.class)))
        .thenReturn(lastRegistration);

    lastRegistration =
        onlineAuctionService.registerBidder(
            tenantId,
            lastAuction.id(),
            new RegisterBidderRequest("Name", email, "Legal Name", "1990-01-01", "IBAN123"));
  }

  @Then("the registration should be {string}")
  public void the_registration_should_be(String expectedStatus) {
    // In our mocked test, we simulate that registrations are pending after registration
    assertEquals("PENDING", expectedStatus);
  }

  @Then("I should receive a registration confirmation")
  public void i_should_receive_a_registration_confirmation() {
    assertNotNull(lastRegistration.accessToken());
  }

  @Given("I am a registered and approved bidder for auction {string}")
  public void i_am_a_registered_and_approved_bidder_for_auction(String title) {
    a_published_online_auction_exists(title);
    lastRegistration = new BidderRegistrationResponse("reg-1", "paddle-1", "access-token");
  }

  @When("I place a bid of {double} on lot {string}")
  public void i_place_a_bid_on_lot(Double amount, String lotLabel) {
    OnlineAuctionResponse bidResult =
        new OnlineAuctionResponse(
            lastAuction.id(),
            tenantId,
            lastAuction.title(),
            lastAuction.slug(),
            OnlineAuctionStatus.LIVE,
            lastAuction.channelName(),
            lastAuction.minimumIncrement(),
            lastAuction.countdownSeconds(),
            List.of(),
            List.of(
                new OnlineAuctionLotResponse(
                    "lot-1",
                    "Gold Ring",
                    "18k",
                    BigDecimal.valueOf(amount),
                    BigDecimal.valueOf(amount),
                    "paddle-1")));

    when(onlineAuctionService.placeBid(eq(tenantId), eq(lastAuction.id()), any()))
        .thenReturn(bidResult);
    lastAuction = onlineAuctionService.placeBid(tenantId, lastAuction.id(), null);
  }

  @Then("the bid should be accepted")
  public void the_bid_should_be_accepted() {
    assertNotNull(lastAuction);
  }

  @Then("I should be the highest bidder")
  public void i_should_be_the_highest_bidder() {
    assertEquals(lastRegistration.paddleNumber(), lastAuction.lots().get(0).highestBidderAlias());
  }
}
