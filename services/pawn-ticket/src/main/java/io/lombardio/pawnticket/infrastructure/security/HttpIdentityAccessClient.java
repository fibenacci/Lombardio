package io.lombardio.pawnticket.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Component
public class HttpIdentityAccessClient implements IdentityAccessClient {

    private static final Logger log = LoggerFactory.getLogger(HttpIdentityAccessClient.class);
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
            log.warn("identity-access rejected bearer token while resolving current user");
            return Optional.empty();
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().is4xxClientError()) {
                log.warn("identity-access returned {} while resolving current user", exception.getStatusCode().value());
                return Optional.empty();
            }
            log.error("identity-access call failed while resolving current user", exception);
            throw exception;
        }
    }
}
