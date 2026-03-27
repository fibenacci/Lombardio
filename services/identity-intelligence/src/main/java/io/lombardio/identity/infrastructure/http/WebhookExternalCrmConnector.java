package io.lombardio.identity.infrastructure.http;

import io.lombardio.identity.domain.model.Customer;
import io.lombardio.identity.domain.port.ExternalCrmConnector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;

@Component
public class WebhookExternalCrmConnector implements ExternalCrmConnector {

    private final RestClient restClient;
    private final String crmBaseUrl;

    public WebhookExternalCrmConnector(
            RestClient.Builder restClientBuilder,
            @Value("${external-crm.base-url:}") String crmBaseUrl
    ) {
        this.crmBaseUrl = crmBaseUrl;
        this.restClient = restClientBuilder.build();
    }

    @Override
    public boolean supports(String tenantId) {
        return crmBaseUrl != null && !crmBaseUrl.isBlank();
    }

    @Override
    public List<Customer> search(String tenantId, String query) {
        if (!supports(tenantId)) {
            return Collections.emptyList();
        }

        try {
            CrmSearchResponse response = restClient.get()
                    .uri(crmBaseUrl + "/api/v1/search?tenantId={tenantId}&q={query}", tenantId, query)
                    .retrieve()
                    .body(CrmSearchResponse.class);

            if (response == null || response.results() == null) {
                return Collections.emptyList();
            }

            return response.results().stream()
                .map(crmCustomer -> new Customer(
                        crmCustomer.id(),
                        tenantId,
                        crmCustomer.customerNumber(),
                        crmCustomer.firstName(),
                        crmCustomer.lastName(),
                        null, // BirthDate parsing if provided by CRM
                        crmCustomer.phone(),
                        crmCustomer.email(),
                        false,
                        "NOT_REQUESTED",
                        crmCustomer.street(),
                        crmCustomer.postalCode(),
                        crmCustomer.city()
                )).toList();

        } catch (Exception e) {
            System.err.println("[ERROR] Failed to query external CRM for tenant " + tenantId + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private record CrmSearchResponse(List<CrmCustomerDto> results) {}

    private record CrmCustomerDto(
            String id,
            String customerNumber,
            String firstName,
            String lastName,
            String phone,
            String email,
            String street,
            String postalCode,
            String city
    ) {}
}
