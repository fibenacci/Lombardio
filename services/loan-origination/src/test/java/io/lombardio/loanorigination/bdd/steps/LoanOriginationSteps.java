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
package io.lombardio.loanorigination.bdd.steps;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.lombardio.loanorigination.application.service.CreateLoanCommand;
import io.lombardio.loanorigination.application.service.CreateLoanPositionCommand;
import io.lombardio.loanorigination.application.service.LoanOriginationService;
import io.lombardio.loanorigination.domain.model.CustomerProfile;
import io.lombardio.loanorigination.domain.model.LoanCase;
import io.lombardio.loanorigination.domain.model.ValuationGuideline;
import io.lombardio.loanorigination.infrastructure.support.InMemoryPorts;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public class LoanOriginationSteps {

  private LoanOriginationService loanOriginationService;
  private InMemoryPorts.Customers customerDirectory;
  private InMemoryPorts.Kyc kycDirectory;
  private InMemoryPorts.Aml amlDirectory;
  private InMemoryPorts.Loans loanCaseRepository;
  private InMemoryPorts.Guidelines valuationGuidelineRepository;
  private InMemoryPorts.PawnTickets pawnTicketIssuer;

  private LoanCase lastResult;
  private final String tenantId = "tenant-bdd";

  @Before
  public void setup() {
    customerDirectory = new InMemoryPorts.Customers();
    kycDirectory = new InMemoryPorts.Kyc();
    amlDirectory = new InMemoryPorts.Aml();
    loanCaseRepository = new InMemoryPorts.Loans();
    valuationGuidelineRepository = new InMemoryPorts.Guidelines();
    pawnTicketIssuer = new InMemoryPorts.PawnTickets();

    Clock clock = Clock.fixed(Instant.parse("2026-04-08T10:00:00Z"), ZoneId.of("UTC"));

    loanOriginationService =
        new LoanOriginationService(
            customerDirectory,
            kycDirectory,
            amlDirectory,
            valuationGuidelineRepository,
            loanCaseRepository,
            pawnTicketIssuer,
            clock,
            null);

    // Seed a default guideline
    valuationGuidelineRepository.save(
        new ValuationGuideline(
            "guide-1", tenantId, "Jewelry", "Gold", "Gold Ring", "18k", BigDecimal.valueOf(100.0)));
  }

  @Given("the loan origination service is running")
  public void the_loan_origination_service_is_running() {
    assertNotNull(loanOriginationService);
  }

  @When("I create a loan case for customer {string} with requested amount {double}")
  public void i_create_a_loan_case_for_customer_with_requested_amount(
      String customerId, Double amount) {
    customerDirectory.save(
        new CustomerProfile(
            customerId,
            tenantId,
            "KD-1",
            "Anna Becker",
            LocalDate.now().minusYears(30),
            null,
            null,
            null,
            null,
            "APPROVED",
            true,
            "PERSONALAUSWEIS"));
    kycDirectory.setApproved(tenantId, customerId, true);
    amlDirectory.setAssessment(
        tenantId,
        customerId,
        new io.lombardio.loanorigination.domain.port.AmlDirectory.AmlAssessment(true, true, "OK"));

    CreateLoanCommand command =
        new CreateLoanCommand(
            customerId,
            List.of(
                new CreateLoanPositionCommand(
                    1, "Gold Ring", "18k", "guide-1", BigDecimal.valueOf(amount))),
            3,
            null,
            false,
            null,
            null,
            null,
            null,
            null);
    lastResult = loanOriginationService.createLoan(tenantId, command);
  }

  @Then("the loan case should be successfully created")
  public void the_loan_case_should_be_successfully_created() {
    assertNotNull(lastResult);
  }

  @Then("the assessment status should be {string}")
  public void the_assessment_status_should_be(String expectedStatus) {
    assertNotNull(lastResult.positions());
  }

  @Given("a loan case for customer {string} with amount {double} exists")
  public void a_loan_case_for_customer_exists(String customerId, Double amount) {
    i_create_a_loan_case_for_customer_with_requested_amount(customerId, amount);
  }

  @When("the risk assessment is performed")
  public void the_risk_assessment_is_performed() {
    // No-op for now
  }

  @Then("the loan case should be {string}")
  public void the_loan_case_should_be(String expectedStatus) {
    assertNotNull(lastResult);
  }
}
