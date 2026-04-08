Feature: Pawn Ticket Lifecycle
  As a pawnshop operator
  I want to issue and manage pawn tickets
  So that I can provide loans to customers against collateral

  Background:
    Given the pawn ticket service is running

  Scenario: Successfully issue a new pawn ticket
    When I issue a pawn ticket for customer "cust-1" with item "Gold Ring" and value 500.00
    Then the pawn ticket should be successfully issued
    And the loan amount should be 500.00
    And the status should be "ACTIVE"

  Scenario: Quote fees for a pawn ticket
    Given an active pawn ticket with loan 100.00 exists
    When I request a quote for the current fees
    Then the quote should include monthly interest and storage fees
