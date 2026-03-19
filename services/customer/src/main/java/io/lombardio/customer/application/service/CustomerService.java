package io.lombardio.customer.application.service;

import io.lombardio.customer.api.http.CreateCustomerRequest;
import io.lombardio.customer.api.http.CustomerResponse;
import io.lombardio.customer.api.http.UpdateCustomerRequest;
import io.lombardio.customer.domain.model.Customer;
import io.lombardio.customer.domain.port.CustomerRepository;
import io.lombardio.customer.domain.port.ExternalCrmConnector;
import io.lombardio.customer.domain.port.KycDirectory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final KycDirectory kycDirectory;
    private final List<ExternalCrmConnector> externalCrmConnectors;

    public CustomerService(
            CustomerRepository customerRepository,
            KycDirectory kycDirectory,
            List<ExternalCrmConnector> externalCrmConnectors
    ) {
        this.customerRepository = customerRepository;
        this.kycDirectory = kycDirectory;
        this.externalCrmConnectors = externalCrmConnectors;
    }

    public List<CustomerResponse> search(String tenantId, String query) {
        List<Customer> result = new ArrayList<>(customerRepository.search(tenantId, query));

        externalCrmConnectors.stream()
                .filter(connector -> connector.supports(tenantId))
                .findFirst()
                .ifPresent(connector -> result.addAll(connector.search(tenantId, query)));

        return result.stream()
                .map(this::toResponse)
                .toList();
    }

    public CustomerResponse create(String tenantId, CreateCustomerRequest request) {
        Customer customer = customerRepository.save(new Customer(
                "customer-" + UUID.randomUUID(),
                tenantId,
                request.customerNumber(),
                request.firstName(),
                request.lastName(),
                request.birthDate(),
                request.phone(),
                request.street(),
                request.postalCode(),
                request.city()
        ));

        return toResponse(customer);
    }

    public CustomerResponse requireById(String tenantId, String customerId) {
        Customer customer = customerRepository.findById(customerId)
                .filter(item -> item.tenantId().equals(tenantId))
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        return toResponse(customer);
    }

    public CustomerResponse update(String tenantId, String customerId, UpdateCustomerRequest request) {
        Customer existing = customerRepository.findById(customerId)
                .filter(item -> item.tenantId().equals(tenantId))
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        Customer updated = customerRepository.save(new Customer(
                existing.id(),
                tenantId,
                request.customerNumber(),
                request.firstName(),
                request.lastName(),
                request.birthDate(),
                request.phone(),
                request.street(),
                request.postalCode(),
                request.city()
        ));

        return toResponse(updated);
    }

    private CustomerResponse toResponse(Customer customer) {
        KycDirectory.KycProjection kyc = kycDirectory.getStatus(customer.tenantId(), customer.id());
        return new CustomerResponse(
                customer.id(),
                customer.customerNumber(),
                customer.firstName(),
                customer.lastName(),
                customer.birthDate(),
                customer.displayName(),
                customer.phone(),
                kyc.status(),
                kyc.approved(),
                kyc.documentType(),
                customer.street(),
                customer.postalCode(),
                customer.city()
        );
    }
}
