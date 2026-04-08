Feature: Loan Origination
  As a pawnshop manager
  I want to assess and create loan cases
  So that I can control the risk and volume of our loan portfolio

  Background:
    Given the loan origination service is running

  Scenario: Successfully create a new loan case
    When I create a loan case for customer "cust-1" with requested amount 2000.00
    Then the loan case should be successfully created
    And the assessment status should be "PENDING"

  Scenario: Assess a loan case for approval
    Given a loan case for customer "cust-2" with amount 500.00 exists
    When the risk assessment is performed
    Then the loan case should be "APPROVED"
