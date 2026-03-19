package io.lombardio.customer.domain.port;

import io.lombardio.customer.domain.model.Customer;

import java.util.List;

public interface ExternalCrmConnector {

    boolean supports(String tenantId);

    List<Customer> search(String tenantId, String query);
}
