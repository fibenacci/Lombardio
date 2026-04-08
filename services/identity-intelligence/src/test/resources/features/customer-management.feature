Feature: Customer Management
  As an operator
  I want to manage customer records
  So that I can identify customers and track their compliance status

  Background:
    Given the identity intelligence service is running

  Scenario: Successfully register a new customer
    When I register a new customer with lastName "Becker" and firstName "Anna"
    Then the customer should be successfully created
    And the customer profile should show KYC status "NOT_STARTED"

  Scenario: Search for existing customers
    Given a customer "KD-1001" with lastName "Schneider" exists
    When I search for customers with query "Schneider"
    Then the search results should include "Schneider"
