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
package io.lombardio.reporting.bdd.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.lombardio.reporting.api.http.ReportingDashboardResponse;
import io.lombardio.reporting.application.service.ReportingService;
import io.lombardio.reporting.domain.port.LoanReadClient;
import io.lombardio.reporting.domain.port.PawnTicketReadClient;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class ReportingSteps {

  private ReportingService reportingService;
  private LoanReadClient loanReadClient;
  private PawnTicketReadClient pawnTicketReadClient;
  private ReportingDashboardResponse lastResponse;
  private List<LoanReadClient.ReportedLoanCase> mockedLoans = new ArrayList<>();
  private List<PawnTicketReadClient.ReportedCashTransaction> mockedTransactions = new ArrayList<>();
  private List<PawnTicketReadClient.ReportedPawnTicketOverview> mockedTickets = new ArrayList<>();
  private final String tenantId = "tenant-bdd";

  @Before
  public void setup() {
    loanReadClient = mock(LoanReadClient.class);
    pawnTicketReadClient = mock(PawnTicketReadClient.class);
    Clock clock = Clock.fixed(Instant.parse("2026-04-08T10:00:00Z"), ZoneOffset.UTC);
    reportingService = new ReportingService(loanReadClient, pawnTicketReadClient, clock);

    mockedLoans.clear();
    mockedTransactions.clear();
    mockedTickets.clear();

    when(loanReadClient.listLoans(anyString(), anyString())).thenReturn(mockedLoans);
    when(pawnTicketReadClient.listCashTransactions(anyString(), anyString()))
        .thenReturn(mockedTransactions);
    when(pawnTicketReadClient.listTickets(anyString(), anyString())).thenReturn(mockedTickets);
  }

  @Given("the reporting service is running")
  public void the_reporting_service_is_running() {
    assertNotNull(reportingService);
  }

  @Given("there are {int} active loan cases with total value {double}")
  public void there_are_active_loan_cases_with_total_value(Integer count, Double totalValue) {
    BigDecimal valuePerTicket = BigDecimal.valueOf(totalValue / count);
    for (int i = 0; i < count; i++) {
      mockedTickets.add(
          new PawnTicketReadClient.ReportedPawnTicketOverview(
              "PS-" + i, valuePerTicket, valuePerTicket.multiply(BigDecimal.valueOf(1.1)), 1));
    }
  }

  @Given("there were {int} cash transactions today with total revenue {double}")
  public void there_were_cash_transactions_today_with_total_revenue(
      Integer count, Double totalRevenue) {
    BigDecimal revenuePerTx = BigDecimal.valueOf(totalRevenue / count);
    for (int i = 0; i < count; i++) {
      mockedTransactions.add(
          new PawnTicketReadClient.ReportedCashTransaction(
              "REDEEM",
              revenuePerTx.multiply(BigDecimal.valueOf(0.5)),
              revenuePerTx.multiply(BigDecimal.valueOf(0.5)),
              revenuePerTx,
              Instant.now()));
    }
  }

  @When("I request the dashboard for the last {int} days")
  public void i_request_the_dashboard_for_the_last_days(Integer days) {
    lastResponse = reportingService.getDashboard(tenantId, days, "fake-token");
  }

  @Then("the dashboard should show active exposure of {double}")
  public void the_dashboard_should_show_active_exposure_of(Double expectedValue) {
    assertEquals(
        0,
        BigDecimal.valueOf(expectedValue).compareTo(lastResponse.finance().activeLoanExposure()));
  }

  @Then("the realized revenue should be {double}")
  public void the_realized_revenue_should_be(Double expectedValue) {
    assertEquals(
        0, BigDecimal.valueOf(expectedValue).compareTo(lastResponse.finance().realizedRevenue()));
  }
}
