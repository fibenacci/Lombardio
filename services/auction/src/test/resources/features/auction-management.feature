Feature: Auction Management
  As an auction manager
  I want to plan and execute auctions
  So that unredeemed collateral can be sold according to legal requirements

  Background:
    Given the auction service is running

  Scenario: Successfully create a new auction draft
    When I create an auction with title "Spring Jewelry Auction" at "Main Branch"
    And I add a lot for contract "PS-5001" with estimated value 1000.00
    Then the auction should be created in status "DRAFT"
    And the auction should contain 1 lot

  Scenario: Announce and execute an auction
    Given an auction draft "Spring Jewelry Auction" exists
    When I announce the auction for "2026-04-16" with reference "ANN-2026-01"
    And I open the auction
    Then the auction status should be "LIVE"
