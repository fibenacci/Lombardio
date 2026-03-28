Feature: Tenant Registration
  As a platform administrator
  I want to register new tenants
  So that I can onboard new pawnshops

  Scenario: Successfully register a new tenant
    Given I am logged in as a platform administrator
    And I am on the tenant management page
    When I enter "Pfandhaus Gold" as the name and "gold" as the key
    And I submit the registration form
    Then the tenant "Pfandhaus Gold" should be visible in the list
    And I should see a success message "Tenant created"
