package io.lombardio.identity.infrastructure.persistence.support;

import io.lombardio.identity.domain.model.Customer;
import io.lombardio.identity.domain.port.CustomerRepository;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class InMemoryCustomerRepository implements CustomerRepository {

    private final Map<String, Customer> store = new LinkedHashMap<>();

    public InMemoryCustomerRepository() {
        save(new Customer("customer-berlin-1", "tenant-default", "KD-1001", "Anna", "Becker", LocalDate.parse("1988-04-12"), "+49 170 111111", "anna@example.test", true, "INVITED", "Hauptstr. 1", "10115", "Berlin"));
        save(new Customer("customer-berlin-2", "tenant-default", "KD-1002", "Murat", "Yilmaz", LocalDate.parse("1985-09-03"), "+49 170 222222", null, false, "NOT_REQUESTED", "Brunnenstr. 20", "10119", "Berlin"));
    }

    @Override
    public List<Customer> search(String tenantId, String query) {
        String normalized = query == null ? "" : query.trim().toLowerCase();
        return store.values().stream()
                .filter(customer -> customer.tenantId().equals(tenantId))
                .filter(customer -> normalized.isBlank()
                        || customer.customerNumber().toLowerCase().contains(normalized)
                        || customer.displayName().toLowerCase().contains(normalized)
                        || customer.phone().toLowerCase().contains(normalized))
                .toList();
    }

    @Override
    public Optional<Customer> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        return store.values().stream()
                .filter(customer -> customer.email() != null)
                .filter(customer -> customer.email().equalsIgnoreCase(email))
                .findFirst();
    }

    @Override
    public Customer save(Customer customer) {
        store.put(customer.id(), customer);
        return customer;
    }
}
