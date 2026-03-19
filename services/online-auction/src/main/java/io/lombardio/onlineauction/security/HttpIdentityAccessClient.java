package io.lombardio.onlineauction.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class HttpIdentityAccessClient implements IdentityAccessClient {

    private final RestClient restClient;

    public HttpIdentityAccessClient(@Value("${identity-access.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<IdentityCurrentUser> fetchCurrentUser(String bearerToken) {
        try {
            Map<String, Object> response = restClient.get()
                    .uri("/api/v1/auth/me")
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve()
                    .body(Map.class);
            if (response == null) {
                return Optional.empty();
            }
            Set<String> permissions = new HashSet<>((List<String>) response.getOrDefault("permissions", List.of()));
            boolean platformManager = permissions.contains("platform.tenants.read") || permissions.contains("platform.tenants.write");
            return Optional.of(new IdentityCurrentUser(
                    (String) response.get("id"),
                    (String) response.get("tenantId"),
                    (String) response.get("email"),
                    (String) response.get("displayName"),
                    permissions,
                    platformManager
            ));
        } catch (HttpClientErrorException exception) {
            return Optional.empty();
        }
    }
}
