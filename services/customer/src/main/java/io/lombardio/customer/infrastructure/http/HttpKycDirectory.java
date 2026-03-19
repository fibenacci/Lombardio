package io.lombardio.customer.infrastructure.http;

import io.lombardio.customer.domain.port.KycDirectory;
import io.lombardio.customer.infrastructure.security.RequestAuthorizationTokenResolver;
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
    public KycProjection getStatus(String tenantId, String customerId) {
        RestClient.RequestHeadersSpec<?> request = restClient.get()
                .uri("/api/v1/tenants/{tenantId}/customers/{customerId}/kyc", tenantId, customerId);
        tokenResolver.resolveBearerToken()
                .ifPresent(token -> request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
        KycRecord record = request.retrieve().body(KycRecord.class);

        if (record == null) {
            return new KycProjection("NOT_STARTED", false, null);
        }

        return new KycProjection(record.status(), "APPROVED".equals(record.status()), record.documentType());
    }

    private record KycRecord(String customerId, String status, String documentType) {
    }
}
