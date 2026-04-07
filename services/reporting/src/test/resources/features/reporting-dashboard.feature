Feature: Reporting Dashboard
  As a pawnshop owner
  I want to see financial reports and trends
  So that I can monitor the business performance and inventory

  Background:
    Given the reporting service is running

  Scenario: Generate financial dashboard for the last 7 days
    Given there are 5 active loan cases with total value 5000.00
    And there were 3 cash transactions today with total revenue 150.00
    When I request the dashboard for the last 7 days
    Then the dashboard should show active exposure of 5000.00
    And the realized revenue should be 150.00
