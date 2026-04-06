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
package io.lombardio.identity.api.http;

import io.lombardio.identity.application.service.CustomerService;
import io.lombardio.identity.application.service.CustomerView;
import io.lombardio.identity.application.service.CreateCustomerCommand;
import io.lombardio.identity.application.service.UpdateCustomerCommand;
import io.lombardio.identity.infrastructure.security.CustomerAuthorizationService;
import io.lombardio.platform.security.AuthenticatedUser;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}")
public class CustomerController {

  private final CustomerService customerService;
  private final CustomerAuthorizationService authorizationService;

  public CustomerController(
      CustomerService customerService, CustomerAuthorizationService authorizationService) {
    this.customerService = customerService;
    this.authorizationService = authorizationService;
  }

  @GetMapping("/customers")
  public List<CustomerView> search(
      @AuthenticationPrincipal AuthenticatedUser principal,
      @PathVariable String tenantId,
      @RequestParam(name = "query", required = false, defaultValue = "") String query) {
    authorizationService.requireRead(principal, tenantId);
    return customerService.search(tenantId, query);
  }

  @PostMapping("/customers")
  public CustomerView create(
      @AuthenticationPrincipal AuthenticatedUser principal,
      @PathVariable String tenantId,
      @Valid @RequestBody CreateCustomerRequest request) {
    authorizationService.requireWrite(principal, tenantId);
    return customerService.create(
        tenantId,
        new CreateCustomerCommand(
            request.customerNumber(),
            request.firstName(),
            request.lastName(),
            request.birthDate(),
            request.phone(),
            request.email(),
            request.wantsDigitalPawnTicket(),
            request.street(),
            request.postalCode(),
            request.city()));
  }

  @GetMapping("/customers/{customerId}")
  public CustomerView getById(
      @AuthenticationPrincipal AuthenticatedUser principal,
      @PathVariable String tenantId,
      @PathVariable String customerId) {
    authorizationService.requireRead(principal, tenantId);
    return customerService.requireById(tenantId, customerId);
  }

  @PutMapping("/customers/{customerId}")
  public CustomerView update(
      @AuthenticationPrincipal AuthenticatedUser principal,
      @PathVariable String tenantId,
      @PathVariable String customerId,
      @Valid @RequestBody UpdateCustomerRequest request) {
    authorizationService.requireWrite(principal, tenantId);
    return customerService.update(
        tenantId,
        customerId,
        new UpdateCustomerCommand(
            request.customerNumber(),
            request.firstName(),
            request.lastName(),
            request.birthDate(),
            request.phone(),
            request.email(),
            request.wantsDigitalPawnTicket(),
            request.street(),
            request.postalCode(),
            request.city()));
  }

  @GetMapping("/health")
  public Map<String, String> health() {
    return Map.of("status", "ok");
  }
}
