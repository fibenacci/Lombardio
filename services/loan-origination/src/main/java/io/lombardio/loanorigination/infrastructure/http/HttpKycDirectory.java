package io.lombardio.loanorigination.infrastructure.http;

import io.lombardio.loanorigination.domain.port.KycDirectory;
import io.lombardio.loanorigination.infrastructure.security.RequestAuthorizationTokenResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class HttpKycDirectory implements KycDirectory {

    private final RestClient restClient;
    private final RequestAuthorizationTokenResolver tokenResolver;

    public HttpKycDirectory(
            RestClient.Builder restClientBuilder,
            @Value("${kyc.base-url:http://localhost:8086}") String kycBaseUrl,
            RequestAuthorizationTokenResolver tokenResolver
    ) {
        this.restClient = restClientBuilder.baseUrl(kycBaseUrl).build();
        this.tokenResolver = tokenResolver;
    }

    @Override
    public boolean isApproved(String tenantId, String customerId) {
        RestClient.RequestHeadersSpec<?> request = restClient.get()
                .uri("/api/v1/tenants/{tenantId}/customers/{customerId}/kyc/approval", tenantId, customerId);
        tokenResolver.resolveBearerToken()
                .ifPresent(token -> request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
        ApprovalResponse response = request.retrieve().body(ApprovalResponse.class);
        return response != null && response.approved();
    }

    private record ApprovalResponse(boolean approved) {
    }
}
