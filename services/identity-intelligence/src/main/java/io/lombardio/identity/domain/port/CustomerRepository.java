package io.lombardio.identity.domain.port;

import io.lombardio.identity.domain.model.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository {

    List<Customer> search(String tenantId, String query);

    Optional<Customer> findById(String id);

    Optional<Customer> findByEmail(String email);

    Customer save(Customer customer);
}
