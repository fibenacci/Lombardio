/*
 * Lombardio Source-Available No-Distribution License 1.0
 *
 * Copyright (c) 2026 Benjamin Letzel. All rights reserved.
 *
 * This project is source-available for educational and review purposes only.
 * Redistribution, sublicensing, or commercial use is strictly prohibited.
 *
 * For partnership or cooperation inquiries, please contact the author.
 */
package io.lombardio.platform.auth.infrastructure.keycloak;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.lombardio.platform.auth.application.OperatorIdentityProvider;
import io.lombardio.platform.auth.application.OperatorIdentityTokens;
import io.lombardio.platform.config.KeycloakProperties;
import io.lombardio.platform.iam.application.IdentityProviderUnavailableException;
import io.lombardio.platform.security.UnauthorizedException;
import java.util.Map;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class OperatorKeycloakIdentityProvider implements OperatorIdentityProvider {

  private final RestClient keycloakOidcRestClient;
  private final KeycloakProperties keycloakProperties;

  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring managed singleton beans")
  public OperatorKeycloakIdentityProvider(
      RestClient keycloakOidcRestClient, KeycloakProperties keycloakProperties) {
    this.keycloakOidcRestClient = keycloakOidcRestClient;
    this.keycloakProperties = keycloakProperties;
  }

  @Override
  public OperatorIdentityTokens login(String email, String password) {
    return exchangeToken(
        formData(
            Map.of(
                "grant_type",
                "password",
                "client_id",
                keycloakProperties.operatorClientId(),
                "username",
                email,
                "password",
                password)),
        "Invalid email or password");
  }

  @Override
  public OperatorIdentityTokens refresh(String refreshToken) {
    return exchangeToken(
        formData(
            Map.of(
                "grant_type",
                "refresh_token",
                "client_id",
                keycloakProperties.operatorClientId(),
                "refresh_token",
                refreshToken)),
        "Session expired");
  }

  @Override
  public void logout(String refreshToken) {
    if (refreshToken == null || refreshToken.isBlank()) {
      return;
    }

    try {
      keycloakOidcRestClient
          .post()
          .uri("/realms/{realm}/protocol/openid-connect/logout", keycloakProperties.realm())
          .contentType(MediaType.APPLICATION_FORM_URLENCODED)
          .body(
              formData(
                  Map.of(
                      "client_id",
                      keycloakProperties.operatorClientId(),
                      "refresh_token",
                      refreshToken)))
          .retrieve()
          .toBodilessEntity();
    } catch (RestClientResponseException exception) {
      if (exception.getStatusCode().value() == 400 || exception.getStatusCode().value() == 401) {
        return;
      }
      throw new IdentityProviderUnavailableException("Operator logout failed", exception);
    } catch (RuntimeException exception) {
      throw new IdentityProviderUnavailableException("Operator logout failed", exception);
    }
  }

  private OperatorIdentityTokens exchangeToken(
      MultiValueMap<String, String> form, String unauthorizedMessage) {
    try {
      KeycloakTokenResponse response =
          keycloakOidcRestClient
              .post()
              .uri("/realms/{realm}/protocol/openid-connect/token", keycloakProperties.realm())
              .contentType(MediaType.APPLICATION_FORM_URLENCODED)
              .body(form)
              .retrieve()
              .body(KeycloakTokenResponse.class);
      return new OperatorIdentityTokens(
          response == null ? null : response.accessToken(),
          response == null ? null : response.refreshToken());
    } catch (RestClientResponseException exception) {
      HttpStatusCode statusCode = exception.getStatusCode();
      if (statusCode.value() == 400 || statusCode.value() == 401) {
        throw new UnauthorizedException(unauthorizedMessage);
      }
      throw new IdentityProviderUnavailableException("Operator authentication failed", exception);
    } catch (RuntimeException exception) {
      throw new IdentityProviderUnavailableException("Operator authentication failed", exception);
    }
  }

  private MultiValueMap<String, String> formData(Map<String, String> values) {
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    values.forEach(form::add);
    return form;
  }
}
