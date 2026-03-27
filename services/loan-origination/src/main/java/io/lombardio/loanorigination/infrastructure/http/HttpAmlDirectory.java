package io.lombardio.loanorigination.infrastructure.http;

import io.lombardio.loanorigination.domain.port.AmlDirectory;
import io.lombardio.platform.security.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;

@Component
public class HttpAmlDirectory implements AmlDirectory {

    private final RestClient restClient;

    public HttpAmlDirectory(
            RestClient.Builder restClientBuilder,
            @Value("${identity.base-url:http://localhost:8084}") String amlBaseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(amlBaseUrl).build();
    }

    @Override
    public AmlAssessment assessForOrigination(String tenantId, String customerId, BigDecimal loanAmount) {
        try {
            RestClient.RequestBodySpec request = restClient.post()
                    .uri("/api/v1/tenants/{tenantId}/customers/{customerId}/aml/origination-check", tenantId, customerId);
            AuthenticatedUser.currentAccessToken()
                    .ifPresent(token -> request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token));

            AmlAssessmentRecord response = request
                    .body(new OriginationAssessmentRequest(loanAmount))
                    .retrieve()
                    .body(AmlAssessmentRecord.class);

            if (response == null) {
                throw new IllegalStateException("AML service returned no payload");
            }

            return new AmlAssessment(
                    response.featureAvailable(),
                    response.originationAllowed(),
                    response.decisionReason()
            );
        } catch (RestClientException exception) {
            throw new IllegalStateException("AML service unavailable", exception);
        }
    }

    private record OriginationAssessmentRequest(BigDecimal loanAmount) {
    }

    private record AmlAssessmentRecord(
            boolean featureAvailable,
            boolean originationAllowed,
            String decisionReason
    ) {
    }
}
