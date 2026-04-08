Feature: Online Auction Platform
  As a bidder
  I want to participate in online auctions
  So that I can bid on items from anywhere

  Background:
    Given the online auction service is running

  Scenario: Successfully register for an online auction
    Given a published online auction "Summer Watches" exists
    When I register as a bidder with email "bidder@example.test"
    Then the registration should be "PENDING"
    And I should receive a registration confirmation

  Scenario: Place a bid on an active auction item
    Given I am a registered and approved bidder for auction "Summer Watches"
    When I place a bid of 1500.00 on lot "LOT-1"
    Then the bid should be accepted
    And I should be the highest bidder
