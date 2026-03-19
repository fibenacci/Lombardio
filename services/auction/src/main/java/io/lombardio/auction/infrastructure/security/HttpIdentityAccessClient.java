package io.lombardio.auction.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class HttpIdentityAccessClient implements IdentityAccessClient {

    private final RestClient restClient;

    public HttpIdentityAccessClient(RestClient.Builder restClientBuilder,
                                    @Value("${identity-access.base-url}") String baseUrl) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public IdentityCurrentUser fetchCurrentUser(String token) {
        try {
            CurrentUserResponse response = restClient.get()
                    .uri("/api/v1/auth/me")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(CurrentUserResponse.class);
            if (response == null) {
                throw new UnauthorizedIdentityAccessException("Missing identity response");
            }
            return new IdentityCurrentUser(
                    response.userId(),
                    response.tenantId(),
                    response.email(),
                    response.displayName(),
                    response.permissions()
            );
        } catch (HttpClientErrorException exception) {
            throw new UnauthorizedIdentityAccessException("Token validation failed", exception);
        }
    }

    private record CurrentUserResponse(
            String userId,
            String tenantId,
            String email,
            String displayName,
            List<String> permissions
    ) {
    }
}
