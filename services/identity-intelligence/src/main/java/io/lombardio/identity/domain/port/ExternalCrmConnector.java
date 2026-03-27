package io.lombardio.identity.domain.port;

import io.lombardio.identity.domain.model.Customer;

import java.util.List;

public interface ExternalCrmConnector {

    boolean supports(String tenantId);

    List<Customer> search(String tenantId, String query);
}
