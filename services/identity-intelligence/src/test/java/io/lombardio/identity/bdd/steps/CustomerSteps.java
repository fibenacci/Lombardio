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
package io.lombardio.identity.bdd.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.lombardio.identity.application.service.CreateCustomerCommand;
import io.lombardio.identity.application.service.CustomerService;
import io.lombardio.identity.application.service.CustomerView;
import io.lombardio.identity.domain.port.KycDirectory;
import io.lombardio.identity.infrastructure.persistence.support.InMemoryCustomerRepository;
import io.lombardio.identity.portal.application.CustomerPortalService;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class CustomerSteps {

  private CustomerService customerService;
  private InMemoryCustomerRepository customerRepository;
  private CustomerView lastResult;
  private List<CustomerView> lastSearchResults;
  private final String tenantId = "tenant-bdd";

  @Before
  public void setup() {
    customerRepository = new InMemoryCustomerRepository();
    KycDirectory kycDirectory =
        (t, c, token) -> new KycDirectory.KycProjection("NOT_STARTED", false, null);
    customerService =
        new CustomerService(
            customerRepository, kycDirectory, List.of(), mock(CustomerPortalService.class));
  }

  @Given("the identity intelligence service is running")
  public void the_identity_intelligence_service_is_running() {
    assertNotNull(customerService);
  }

  @When("I register a new customer with lastName {string} and firstName {string}")
  public void i_register_a_new_customer_with_last_name_and_first_name(
      String lastName, String firstName) {
    CreateCustomerCommand command =
        new CreateCustomerCommand(
            "KD-NEW",
            firstName,
            lastName,
            LocalDate.now().minusYears(30),
            null,
            null,
            false,
            null,
            null,
            null);
    lastResult = customerService.create(tenantId, command, Optional.empty());
  }

  @Then("the customer should be successfully created")
  public void the_customer_should_be_successfully_created() {
    assertNotNull(lastResult);
    assertNotNull(lastResult.id());
  }

  @Then("the customer profile should show KYC status {string}")
  public void the_customer_profile_should_show_kyc_status(String expectedStatus) {
    assertEquals(expectedName(expectedStatus), lastResult.kycStatus());
  }

  @Given("a customer {string} with lastName {string} exists")
  public void a_customer_with_last_name_exists(String customerNumber, String lastName) {
    CreateCustomerCommand command =
        new CreateCustomerCommand(
            customerNumber,
            "Test",
            lastName,
            LocalDate.now().minusYears(30),
            null,
            null,
            false,
            null,
            null,
            null);
    customerService.create(tenantId, command, Optional.empty());
  }

  @When("I search for customers with query {string}")
  public void i_search_for_customers_with_query(String query) {
    lastSearchResults = customerService.search(tenantId, query, Optional.empty());
  }

  @Then("the search results should include {string}")
  public void the_search_results_should_include(String expectedName) {
    assertTrue(lastSearchResults.stream().anyMatch(c -> c.displayName().contains(expectedName)));
  }

  private String expectedName(String status) {
    return status;
  }
}
