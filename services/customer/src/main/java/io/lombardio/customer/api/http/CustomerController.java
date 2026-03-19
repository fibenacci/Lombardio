package io.lombardio.customer.api.http;

import io.lombardio.customer.application.service.CustomerService;
import io.lombardio.customer.infrastructure.security.AuthenticatedCustomerUser;
import io.lombardio.customer.infrastructure.security.CustomerAuthorizationService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}")
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerAuthorizationService authorizationService;

    public CustomerController(
            CustomerService customerService,
            CustomerAuthorizationService authorizationService
    ) {
        this.customerService = customerService;
        this.authorizationService = authorizationService;
    }

    @GetMapping("/customers")
    public List<CustomerResponse> search(
            @AuthenticationPrincipal AuthenticatedCustomerUser principal,
            @PathVariable String tenantId,
            @RequestParam(name = "query", required = false, defaultValue = "") String query
    ) {
        authorizationService.requireRead(principal, tenantId);
        return customerService.search(tenantId, query);
    }

    @PostMapping("/customers")
    public CustomerResponse create(
            @AuthenticationPrincipal AuthenticatedCustomerUser principal,
            @PathVariable String tenantId,
            @Valid @RequestBody CreateCustomerRequest request
    ) {
        authorizationService.requireWrite(principal, tenantId);
        return customerService.create(tenantId, request);
    }

    @GetMapping("/customers/{customerId}")
    public CustomerResponse getById(
            @AuthenticationPrincipal AuthenticatedCustomerUser principal,
            @PathVariable String tenantId,
            @PathVariable String customerId
    ) {
        authorizationService.requireRead(principal, tenantId);
        return customerService.requireById(tenantId, customerId);
    }

    @PutMapping("/customers/{customerId}")
    public CustomerResponse update(
            @AuthenticationPrincipal AuthenticatedCustomerUser principal,
            @PathVariable String tenantId,
            @PathVariable String customerId,
            @Valid @RequestBody UpdateCustomerRequest request
    ) {
        authorizationService.requireWrite(principal, tenantId);
        return customerService.update(tenantId, customerId, request);
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}
