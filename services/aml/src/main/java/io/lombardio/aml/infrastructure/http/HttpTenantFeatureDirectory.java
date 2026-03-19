package io.lombardio.aml.infrastructure.http;

import io.lombardio.aml.domain.port.TenantFeatureDirectory;
import io.lombardio.aml.infrastructure.security.RequestAuthorizationTokenResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class HttpTenantFeatureDirectory implements TenantFeatureDirectory {

    private final RestClient restClient;
    private final RequestAuthorizationTokenResolver tokenResolver;

    public HttpTenantFeatureDirectory(
            RestClient.Builder restClientBuilder,
            @Value("${platform.base-url:http://localhost:8082}") String platformBaseUrl,
            RequestAuthorizationTokenResolver tokenResolver
    ) {
        this.restClient = restClientBuilder.baseUrl(platformBaseUrl).build();
        this.tokenResolver = tokenResolver;
    }

    @Override
    public boolean isFeatureEnabled(String tenantId, String featureKey) {
        try {
            RestClient.RequestHeadersSpec<?> request = restClient.get()
                    .uri("/api/v1/platform/tenants/{tenantId}/features", tenantId);
            tokenResolver.resolveBearerToken()
                    .ifPresent(token -> request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token));

            TenantFeatureRecord[] response = request.retrieve()
                    .body(TenantFeatureRecord[].class);
            if (response == null) {
                return false;
            }
            for (TenantFeatureRecord record : response) {
                if (featureKey.equals(record.featureKey()) && record.enabled()) {
                    return true;
                }
            }
            return false;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().is4xxClientError()) {
                return false;
            }
            throw exception;
        }
    }

    private record TenantFeatureRecord(
            String tenantId,
            String featureKey,
            boolean enabled
    ) {
    }
}
