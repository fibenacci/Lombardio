Feature: Tenant Management
  As a system administrator
  I want to manage tenants
  So that I can onboard new pawnshops to the platform

  Scenario: Successfully register a new tenant
    Given the platform service is running
    When I request to register a new tenant with name "Gold & Silver Pawn" and slug "gold-silver"
    Then the tenant should be successfully created
    And the tenant "Gold & Silver Pawn" should be available in the system
