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
package io.lombardio.platform.bdd.steps;

import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.lombardio.platform.iam.application.KeycloakService;
import io.lombardio.platform.integration.application.PlatformOutboxService;
import io.lombardio.platform.tenant.api.CreateTenantRequest;
import io.lombardio.platform.tenant.api.TenantResponse;
import io.lombardio.platform.tenant.application.TenantBranchService;
import io.lombardio.platform.tenant.application.TenantCatalogService;
import io.lombardio.platform.tenant.application.TenantFeatureService;
import io.lombardio.platform.tenant.application.TenantLifecycleService;
import io.lombardio.platform.tenant.application.TenantUserService;
import io.lombardio.platform.tenant.application.support.InMemoryTenantRepositories;
import java.time.Clock;
import java.util.List;
import org.junit.jupiter.api.Assertions;

public class TenantSteps {

  private final InMemoryTenantRepositories.Tenants tenants =
      new InMemoryTenantRepositories.Tenants();
  private final InMemoryTenantRepositories.Features features =
      new InMemoryTenantRepositories.Features();
  private final InMemoryTenantRepositories.Branches branches =
      new InMemoryTenantRepositories.Branches();
  private final InMemoryTenantRepositories.OutboxEvents outboxEvents =
      new InMemoryTenantRepositories.OutboxEvents();
  private final KeycloakService keycloakService = mock(KeycloakService.class);

  private TenantCatalogService tenantCatalogService;
  private TenantResponse lastResponse;

  @Before
  public void setup() {
    ObjectMapper objectMapper = new ObjectMapper();
    Clock clock = Clock.systemUTC();
    PlatformOutboxService outboxService = new PlatformOutboxService(outboxEvents, clock);

    TenantLifecycleService tenantLifecycleService =
        new TenantLifecycleService(tenants, outboxService, keycloakService, objectMapper, clock);

    TenantFeatureService tenantFeatureService =
        new TenantFeatureService(
            features, tenantLifecycleService, outboxService, objectMapper, clock);

    TenantBranchService tenantBranchService =
        new TenantBranchService(branches, tenantLifecycleService, clock);

    TenantUserService tenantUserService =
        new TenantUserService(keycloakService, tenantLifecycleService, tenantBranchService);

    tenantCatalogService =
        new TenantCatalogService(
            tenantLifecycleService, tenantFeatureService, tenantBranchService, tenantUserService);
  }

  @Given("the platform service is running")
  public void the_platform_service_is_running() {
    // In this unit-style BDD, service is running if it's initialized
    Assertions.assertNotNull(tenantCatalogService);
  }

  @When("I request to register a new tenant with name {string} and slug {string}")
  public void i_request_to_register_a_new_tenant_with_name_and_slug(String name, String slug) {
    lastResponse = tenantCatalogService.createTenant(new CreateTenantRequest(slug, name, "ACTIVE"));
  }

  @Then("the tenant should be successfully created")
  public void the_tenant_should_be_successfully_created() {
    Assertions.assertNotNull(lastResponse);
  }

  @Then("the tenant {string} should be available in the system")
  public void the_tenant_should_be_available_in_the_system(String expectedName) {
    List<TenantResponse> allTenants = tenantCatalogService.listTenants();
    boolean exists = allTenants.stream().anyMatch(t -> t.displayName().equals(expectedName));
    Assertions.assertTrue(exists, "Tenant with name " + expectedName + " should exist");
  }
}
