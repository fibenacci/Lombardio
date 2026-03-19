package io.lombardio.kyc.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Optional;

@Component
public class HttpIdentityAccessClient implements IdentityAccessClient {

    private final RestClient restClient;

    public HttpIdentityAccessClient(
            RestClient.Builder restClientBuilder,
            @Value("${identity-access.base-url:http://localhost:8081}") String identityAccessBaseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(identityAccessBaseUrl).build();
    }

    @Override
    public Optional<IdentityCurrentUser> currentUser(String token) {
        try {
            IdentityCurrentUser currentUser = restClient.get()
                    .uri("/api/v1/auth/me")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        throw new UnauthorizedIdentityAccessException();
                    })
                    .body(IdentityCurrentUser.class);

            return Optional.ofNullable(currentUser);
        } catch (UnauthorizedIdentityAccessException exception) {
            return Optional.empty();
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().is4xxClientError()) {
                return Optional.empty();
            }
            throw exception;
        }
    }
}
