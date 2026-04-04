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
package io.lombardio.platform.auth.application;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.lombardio.platform.auth.api.OperatorSessionUserResponse;
import io.lombardio.platform.config.KeycloakProperties;
import io.lombardio.platform.iam.application.IdentityProviderUnavailableException;
import io.lombardio.platform.security.AuthenticatedUser;
import io.lombardio.platform.security.UnauthorizedException;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class OperatorAuthService {

  private final RestClient keycloakOidcRestClient;
  private final JwtDecoder jwtDecoder;
  private final KeycloakProperties keycloakProperties;

  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP2",
      justification = "Standard Spring dependency injection")
  public OperatorAuthService(
      RestClient keycloakOidcRestClient,
      JwtDecoder jwtDecoder,
      KeycloakProperties keycloakProperties) {
    this.keycloakOidcRestClient = keycloakOidcRestClient;
    this.jwtDecoder = jwtDecoder;
    this.keycloakProperties = keycloakProperties;
  }

  public OperatorSession login(String email, String password) {
    KeycloakTokenResponse response =
        exchangeToken(
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
    return toOperatorSession(response);
  }

  public OperatorSession refresh(String refreshToken) {
    KeycloakTokenResponse response =
        exchangeToken(
            formData(
                Map.of(
                    "grant_type",
                    "refresh_token",
                    "client_id",
                    keycloakProperties.operatorClientId(),
                    "refresh_token",
                    refreshToken)),
            "Session expired");
    return toOperatorSession(response);
  }

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

  public AuthenticatedUser authenticatedUserFromAccessToken(String accessToken) {
    try {
      Jwt jwt = jwtDecoder.decode(accessToken);
      List<String> permissions = extractPermissions(jwt);
      return new AuthenticatedUser(
          jwt.getSubject(),
          stringClaim(jwt, "actorUserId", jwt.getSubject()),
          stringClaim(jwt, "tenantId", null),
          booleanClaim(jwt, "impersonating"),
          stringClaim(jwt, "email", stringClaim(jwt, "preferred_username", "")),
          stringClaim(
              jwt,
              "name",
              stringClaim(jwt, "preferred_username", stringClaim(jwt, "email", jwt.getSubject()))),
          permissions);
    } catch (RuntimeException exception) {
      throw new UnauthorizedException("Invalid operator session");
    }
  }

  public OperatorSessionUserResponse profileFromAccessToken(String accessToken) {
    AuthenticatedUser user = authenticatedUserFromAccessToken(accessToken);
    return OperatorSessionUserResponse.fromAuthenticatedUser(user);
  }

  private OperatorSession toOperatorSession(KeycloakTokenResponse response) {
    if (response == null
        || response.accessToken() == null
        || response.accessToken().isBlank()
        || response.refreshToken() == null
        || response.refreshToken().isBlank()) {
      throw new IdentityProviderUnavailableException(
          "Operator authentication failed", new IllegalStateException("Incomplete token response"));
    }

    OperatorSessionUserResponse user = profileFromAccessToken(response.accessToken());
    return new OperatorSession(response.accessToken(), response.refreshToken(), user);
  }

  private KeycloakTokenResponse exchangeToken(
      MultiValueMap<String, String> form, String unauthorizedMessage) {
    try {
      return keycloakOidcRestClient
          .post()
          .uri("/realms/{realm}/protocol/openid-connect/token", keycloakProperties.realm())
          .contentType(MediaType.APPLICATION_FORM_URLENCODED)
          .body(form)
          .retrieve()
          .body(KeycloakTokenResponse.class);
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

  @SuppressWarnings("unchecked")
  private List<String> extractPermissions(Jwt jwt) {
    Object realmAccess = jwt.getClaims().get("realm_access");
    if (realmAccess instanceof Map<?, ?> map) {
      Object roles = map.get("roles");
      if (roles instanceof List<?> list) {
        return list.stream().filter(String.class::isInstance).map(String.class::cast).toList();
      }
    }
    return List.of();
  }

  private String stringClaim(Jwt jwt, String claimName, String fallback) {
    Object value = jwt.getClaims().get(claimName);
    return value instanceof String stringValue ? stringValue : fallback;
  }

  private boolean booleanClaim(Jwt jwt, String claimName) {
    Object value = jwt.getClaims().get(claimName);
    return value instanceof Boolean booleanValue && booleanValue;
  }
}
