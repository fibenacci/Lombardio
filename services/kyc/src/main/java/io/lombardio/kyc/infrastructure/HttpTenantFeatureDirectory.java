package io.lombardio.kyc.infrastructure;

import io.lombardio.kyc.domain.TenantFeatureDirectory;
import io.lombardio.kyc.security.RequestAuthorizationTokenResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

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
        RestClient.RequestHeadersSpec<?> request = restClient.get()
                .uri("/api/v1/platform/tenants/{tenantId}/features", tenantId);
        tokenResolver.resolveBearerToken()
                .ifPresent(token -> request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token));

        try {
            TenantFeatureResponse[] features = request.retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, response) -> {
                        throw new RestClientResponseException("Feature lookup failed", response.getStatusCode().value(), "", null, null, null);
                    })
                    .body(TenantFeatureResponse[].class);

            if (features == null) {
                return false;
            }

            return List.of(features).stream()
                    .anyMatch(feature -> feature.featureKey().equals(featureKey) && feature.enabled());
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().is4xxClientError()) {
                return false;
            }
            throw exception;
        }
    }

    private record TenantFeatureResponse(String featureKey, boolean enabled) {
    }
}
