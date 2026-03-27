package io.lombardio.identity.application.service;

import io.lombardio.identity.api.http.CreateCustomerRequest;
import io.lombardio.identity.api.http.CustomerResponse;
import io.lombardio.identity.api.http.UpdateCustomerRequest;
import io.lombardio.identity.domain.model.Customer;
import io.lombardio.identity.domain.port.CustomerRepository;
import io.lombardio.identity.domain.port.ExternalCrmConnector;
import io.lombardio.identity.domain.port.KycDirectory;
import io.lombardio.identity.portal.application.CustomerPortalService;
import io.lombardio.platform.security.AuthenticatedUser;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
            CustomerPortalService customerPortalService
    ) {
        this.customerRepository = customerRepository;
        this.kycDirectory = kycDirectory;
        this.externalCrmConnectors = externalCrmConnectors;
        this.customerPortalService = customerPortalService;
    }

    public List<CustomerResponse> search(String tenantId, String query) {
        List<Customer> result = new ArrayList<>(customerRepository.search(tenantId, query));

        externalCrmConnectors.stream()
                .filter(connector -> connector.supports(tenantId))
                .findFirst()
                .ifPresent(connector -> result.addAll(connector.search(tenantId, query)));

        Optional<String> accessToken = AuthenticatedUser.currentAccessToken();

        return result.stream()
                .map(customer -> toResponse(customer, accessToken))
                .toList();
    }

    public CustomerResponse create(String tenantId, CreateCustomerRequest request) {
        String email = normalizeEmail(request.email());
        validateDigitalPawnTicketRequest(email, request.wantsDigitalPawnTicket());
        Customer customer = customerRepository.save(new Customer(
                "customer-" + UUID.randomUUID(),
                tenantId,
                request.customerNumber(),
                request.firstName(),
                request.lastName(),
                request.birthDate(),
                request.phone(),
                email,
                request.wantsDigitalPawnTicket(),
                request.wantsDigitalPawnTicket() ? "INVITED" : "NOT_REQUESTED",
                request.street(),
                request.postalCode(),
                request.city()
        ));
        if (customer.wantsDigitalPawnTicket()) {
            customerPortalService.issueInvitation(customer);
        } else {
            customerPortalService.disableAccess(customer);
        }

        return toResponse(customer, AuthenticatedUser.currentAccessToken());
    }

    public CustomerResponse requireById(String tenantId, String customerId) {
        Customer customer = customerRepository.findById(customerId)
                .filter(item -> item.tenantId().equals(tenantId))
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        return toResponse(customer, AuthenticatedUser.currentAccessToken());
    }

    public CustomerResponse update(String tenantId, String customerId, UpdateCustomerRequest request) {
        Customer existing = customerRepository.findById(customerId)
                .filter(item -> item.tenantId().equals(tenantId))
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        String email = normalizeEmail(request.email());
        validateDigitalPawnTicketRequest(email, request.wantsDigitalPawnTicket());

        boolean emailChanged = !java.util.Objects.equals(existing.email(), email);
        String onlineAccessStatus = determineOnlineAccessStatus(existing, request.wantsDigitalPawnTicket(), emailChanged);

        Customer updated = customerRepository.save(new Customer(
                existing.id(),
                tenantId,
                request.customerNumber(),
                request.firstName(),
                request.lastName(),
                request.birthDate(),
                request.phone(),
                email,
                request.wantsDigitalPawnTicket(),
                onlineAccessStatus,
                request.street(),
                request.postalCode(),
                request.city()
        ));
        if (updated.wantsDigitalPawnTicket()) {
            if (!existing.wantsDigitalPawnTicket() || emailChanged || !"ACTIVE".equals(existing.onlineAccessStatus())) {
                customerPortalService.issueInvitation(updated);
            }
        } else {
            customerPortalService.disableAccess(updated);
        }

        return toResponse(updated, AuthenticatedUser.currentAccessToken());
    }

    private CustomerResponse toResponse(Customer customer, Optional<String> accessToken) {
        KycDirectory.KycProjection kyc = kycDirectory.getStatus(customer.tenantId(), customer.id(), accessToken);
        return new CustomerResponse(
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
                customer.city()
        );
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String normalized = email.trim().toLowerCase();
        return normalized.isEmpty() ? null : normalized;
    }

    private String determineOnlineAccessStatus(Customer existing, boolean wantsDigitalPawnTicket, boolean emailChanged) {
        if (!wantsDigitalPawnTicket) {
            return "NOT_REQUESTED";
        }
        if (emailChanged) {
            return "INVITED";
        }
        if (existing.wantsDigitalPawnTicket() && existing.onlineAccessStatus() != null && !existing.onlineAccessStatus().isBlank()) {
            return existing.onlineAccessStatus();
        }
        return "INVITED";
    }

    private void validateDigitalPawnTicketRequest(String email, boolean wantsDigitalPawnTicket) {
        if (wantsDigitalPawnTicket && (email == null || email.isBlank())) {
            throw new IllegalArgumentException("Digital pawn ticket access requires a customer email address");
        }
    }
}
