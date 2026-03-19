package io.lombardio.customer.application.service;

import io.lombardio.customer.api.http.CreateCustomerRequest;
import io.lombardio.customer.api.http.UpdateCustomerRequest;
import io.lombardio.customer.domain.port.KycDirectory;
import io.lombardio.customer.infrastructure.persistence.support.InMemoryCustomerRepository;
import io.lombardio.customer.portal.application.CustomerPortalService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomerServiceTest {

    private final CustomerService customerService = new CustomerService(
            new InMemoryCustomerRepository(),
            (tenantId, customerId) -> new KycDirectory.KycProjection("APPROVED", true, "PERSONALAUSWEIS"),
            java.util.List.of(),
            Mockito.mock(CustomerPortalService.class)
    );

    @Test
    void shouldSearchCustomersWithinTenant() {
        var customers = customerService.search("tenant-default", "Anna");

        assertEquals(1, customers.size());
        assertEquals("Anna Becker", customers.get(0).displayName());
    }

    @Test
    void shouldCreateCustomer() {
        var created = customerService.create("tenant-default", new CreateCustomerRequest(
                "KD-3001",
                "Lena",
                "Sommer",
                LocalDate.parse("1991-05-18"),
                "+49 170 333333",
                null,
                false,
                "Beispielweg 3",
                "10405",
                "Berlin"
        ));

        assertEquals("KD-3001", created.customerNumber());
        assertEquals("Lena Sommer", created.displayName());
        assertEquals("Lena", created.firstName());
        assertEquals(LocalDate.parse("1991-05-18"), created.birthDate());
    }

    @Test
    void shouldLoadCustomerByIdWithinTenant() {
        var customer = customerService.requireById("tenant-default", "customer-berlin-1");

        assertEquals("Anna Becker", customer.displayName());
    }

    @Test
    void shouldUpdateCustomerWithinTenant() {
        var updated = customerService.update("tenant-default", "customer-berlin-1", new UpdateCustomerRequest(
                "KD-1001",
                "Anna",
                "Schneider",
                LocalDate.parse("1988-04-12"),
                "+49 170 999999",
                "anna.schneider@example.test",
                true,
                "Neue Str. 9",
                "10117",
                "Berlin"
        ));

        assertEquals("Anna Schneider", updated.displayName());
        assertEquals("+49 170 999999", updated.phone());
        assertEquals("Neue Str. 9", updated.street());
    }
}
