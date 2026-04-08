Feature: Operator Authentication
  As an operator
  I want to authenticate with the platform
  So that I can securely manage pawnshop operations

  Background:
    Given the platform service is running

  Scenario: Successful login with valid credentials
    When I login with email "admin@lombardio.local" and password "password"
    Then I should receive a valid session ID
    And my profile should be available in the response
    And my display name should be "Admin"

  Scenario: Resolve current operator profile
    Given I am authenticated as "admin@lombardio.local" with display name "Admin"
    When I request my own profile information
    Then the system should return my display name "Admin" and email "admin@lombardio.local"
    And my assigned permissions should be included
