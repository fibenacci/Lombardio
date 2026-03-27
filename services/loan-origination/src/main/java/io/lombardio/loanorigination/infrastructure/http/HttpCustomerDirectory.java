package io.lombardio.loanorigination.infrastructure.http;

import io.lombardio.loanorigination.domain.model.CustomerProfile;
import io.lombardio.loanorigination.domain.port.CustomerDirectory;
import io.lombardio.platform.security.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;

@Component
public class HttpCustomerDirectory implements CustomerDirectory {

    private final RestClient restClient;

    public HttpCustomerDirectory(
            RestClient.Builder restClientBuilder,
            @Value("${identity.base-url:http://localhost:8084}") String customerBaseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(customerBaseUrl).build();
    }

    @Override
    public CustomerProfile requireById(String tenantId, String customerId) {
        try {
            RestClient.RequestHeadersSpec<?> request = restClient.get()
                    .uri("/api/v1/tenants/{tenantId}/customers/{customerId}", tenantId, customerId);
            AuthenticatedUser.currentAccessToken()
                    .ifPresent(token -> request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
            CustomerRecord customer = request.retrieve().body(CustomerRecord.class);

            if (customer == null) {
                throw new IllegalArgumentException("Customer not found");
            }

            return new CustomerProfile(
                    customer.id(),
                    tenantId,
                    customer.customerNumber(),
                    customer.displayName(),
                    customer.birthDate(),
                    customer.phone(),
                    customer.street(),
                    customer.postalCode(),
                    customer.city(),
                    customer.kycStatus(),
                    customer.kycApproved(),
                    customer.checkedDocumentType()
            );
        } catch (RestClientException exception) {
            throw new IllegalStateException("Customer service unavailable", exception);
        }
    }

    private record CustomerRecord(
            String id,
            String customerNumber,
            String displayName,
            LocalDate birthDate,
            String phone,
            String kycStatus,
            boolean kycApproved,
            String checkedDocumentType,
            String street,
            String postalCode,
            String city
    ) {
    }
}
