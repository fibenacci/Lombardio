package io.lombardio.customer.infrastructure.http;

import io.lombardio.customer.domain.model.Customer;
import io.lombardio.customer.domain.port.ExternalCrmConnector;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NoopExternalCrmConnector implements ExternalCrmConnector {

    @Override
    public boolean supports(String tenantId) {
        return false;
    }

    @Override
    public List<Customer> search(String tenantId, String query) {
        return List.of();
    }
}
