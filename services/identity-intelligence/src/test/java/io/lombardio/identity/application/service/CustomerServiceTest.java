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

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.lombardio.identity.domain.port.KycDirectory;
import io.lombardio.identity.infrastructure.persistence.support.InMemoryCustomerRepository;
import io.lombardio.identity.portal.application.CustomerPortalService;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class CustomerServiceTest {

  private final CustomerService customerService =
      new CustomerService(
          new InMemoryCustomerRepository(),
          new KycDirectory() {
            @Override
            public KycProjection getStatus(
                String tenantId, String customerId, Optional<String> accessToken) {
              return new KycProjection("APPROVED", true, "PERSONALAUSWEIS");
            }
          },
          java.util.List.of(),
          Mockito.mock(CustomerPortalService.class));

  @Test
  void shouldSearchCustomersWithinTenant() {
    var customers = customerService.search("tenant-default", "Anna", Optional.empty());

    assertEquals(1, customers.size());
    assertEquals("Anna Becker", customers.get(0).displayName());
  }

  @Test
  void shouldCreateCustomer() {
    var created =
        customerService.create(
            "tenant-default",
            new CreateCustomerCommand(
                "KD-3001",
                "Lena",
                "Sommer",
                LocalDate.parse("1991-05-18"),
                "+49 170 333333",
                null,
                false,
                "Beispielweg 3",
                "10405",
                "Berlin"),
            Optional.empty());

    assertEquals("KD-3001", created.customerNumber());
    assertEquals("Lena Sommer", created.displayName());
    assertEquals("Lena", created.firstName());
    assertEquals(LocalDate.parse("1991-05-18"), created.birthDate());
  }

  @Test
  void shouldLoadCustomerByIdWithinTenant() {
    var customer =
        customerService.requireById("tenant-default", "customer-berlin-1", Optional.empty());

    assertEquals("Anna Becker", customer.displayName());
  }

  @Test
  void shouldUpdateCustomerWithinTenant() {
    var updated =
        customerService.update(
            "tenant-default",
            "customer-berlin-1",
            new UpdateCustomerCommand(
                "KD-1001",
                "Anna",
                "Schneider",
                LocalDate.parse("1988-04-12"),
                "+49 170 999999",
                "anna.schneider@example.test",
                true,
                "Neue Str. 9",
                "10117",
                "Berlin"),
            Optional.empty());

    assertEquals("Anna Schneider", updated.displayName());
    assertEquals("+49 170 999999", updated.phone());
    assertEquals("Neue Str. 9", updated.street());
  }
}
