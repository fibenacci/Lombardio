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
package io.lombardio.identity.application.service;

import io.lombardio.identity.domain.model.Customer;
import io.lombardio.identity.domain.port.CustomerRepository;
import io.lombardio.identity.domain.port.ExternalCrmConnector;
import io.lombardio.identity.domain.port.KycDirectory;
import io.lombardio.identity.portal.application.CustomerPortalService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

  private final CustomerRepository customerRepository;
  private final KycDirectory kycDirectory;
  private final List<ExternalCrmConnector> externalCrmConnectors;
  private final CustomerPortalService customerPortalService;

  public CustomerService(
      CustomerRepository customerRepository,
      KycDirectory kycDirectory,
      List<ExternalCrmConnector> externalCrmConnectors,
      CustomerPortalService customerPortalService) {
    this.customerRepository = customerRepository;
    this.kycDirectory = kycDirectory;
    this.externalCrmConnectors = List.copyOf(externalCrmConnectors);
    this.customerPortalService = customerPortalService;
  }

  public List<CustomerView> search(String tenantId, String query, Optional<String> accessToken) {
    List<Customer> result = new ArrayList<>(customerRepository.search(tenantId, query));

    externalCrmConnectors.stream()
        .filter(connector -> connector.supports(tenantId))
        .findFirst()
        .ifPresent(connector -> result.addAll(connector.search(tenantId, query)));

    return result.stream().map(customer -> toResponse(customer, accessToken)).toList();
  }

  public CustomerView create(
      String tenantId, CreateCustomerCommand request, Optional<String> accessToken) {
    Customer customer =
        customerRepository.save(
            new Customer(
                "customer-" + UUID.randomUUID(),
                tenantId,
                request.customerNumber(),
                request.firstName(),
                request.lastName(),
                request.birthDate(),
                request.phone(),
                request.email(),
                request.wantsDigitalPawnTicket(),
                request.wantsDigitalPawnTicket() ? "INVITED" : "NOT_REQUESTED",
                request.street(),
                request.postalCode(),
                request.city()));
    if (customer.wantsDigitalPawnTicket()) {
      customerPortalService.issueInvitation(customer);
    } else {
      customerPortalService.disableAccess(customer);
    }

    return toResponse(customer, accessToken);
  }

  public CustomerView requireById(
      String tenantId, String customerId, Optional<String> accessToken) {
    Customer customer =
        customerRepository
            .findById(customerId)
            .filter(item -> item.tenantId().equals(tenantId))
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
    return toResponse(customer, accessToken);
  }

  public CustomerView update(
      String tenantId,
      String customerId,
      UpdateCustomerCommand request,
      Optional<String> accessToken) {
    Customer existing =
        customerRepository
            .findById(customerId)
            .filter(item -> item.tenantId().equals(tenantId))
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

    Customer updated =
        existing.update(
            request.customerNumber(),
            request.firstName(),
            request.lastName(),
            request.birthDate(),
            request.phone(),
            request.email(),
            request.wantsDigitalPawnTicket(),
            request.street(),
            request.postalCode(),
            request.city());

    customerRepository.save(updated);

    if (updated.wantsDigitalPawnTicket()) {
      if (!existing.wantsDigitalPawnTicket()
          || !java.util.Objects.equals(existing.email(), updated.email())
          || !"ACTIVE".equals(existing.onlineAccessStatus())) {
        customerPortalService.issueInvitation(updated);
      }
    } else {
      customerPortalService.disableAccess(updated);
    }

    return toResponse(updated, accessToken);
  }

  private CustomerView toResponse(Customer customer, Optional<String> accessToken) {
    KycDirectory.KycProjection kyc =
        kycDirectory.getStatus(customer.tenantId(), customer.id(), accessToken);
    return new CustomerView(
        customer.id(),
        customer.customerNumber(),
        customer.firstName(),
        customer.lastName(),
        customer.birthDate(),
        customer.displayName(),
        customer.phone(),
        customer.email(),
        customer.wantsDigitalPawnTicket(),
        customer.onlineAccessStatus(),
        kyc.status(),
        kyc.approved(),
        kyc.documentType(),
        customer.street(),
        customer.postalCode(),
        customer.city());
  }
}
