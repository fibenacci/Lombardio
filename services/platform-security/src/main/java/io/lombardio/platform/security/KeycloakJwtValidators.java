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
package io.lombardio.platform.security;

import java.util.List;
import java.util.Set;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;

public final class KeycloakJwtValidators {

  private static final Set<String> TRUSTED_ISSUERS =
      Set.of("http://localhost:8080/realms/lombardio", "http://keycloak:8080/realms/lombardio");

  private KeycloakJwtValidators() {}

  public static OAuth2TokenValidator<Jwt> operatorAccessTokenValidator(
      String expectedAuthorizedParty) {
    return new DelegatingOAuth2TokenValidator<>(
        new JwtTimestampValidator(),
        issuerValidator(),
        authorizedPartyValidator(expectedAuthorizedParty));
  }

  private static OAuth2TokenValidator<Jwt> issuerValidator() {
    return jwt -> {
      String issuer = jwt.getIssuer() != null ? jwt.getIssuer().toString() : "";
      if (TRUSTED_ISSUERS.contains(issuer)) {
        return OAuth2TokenValidatorResult.success();
      }
      return OAuth2TokenValidatorResult.failure(
          new OAuth2Error("invalid_issuer", "The issuer is not trusted: " + issuer, null));
    };
  }

  private static OAuth2TokenValidator<Jwt> authorizedPartyValidator(
      String expectedAuthorizedParty) {
    return jwt -> {
      if (expectedAuthorizedParty == null || expectedAuthorizedParty.isBlank()) {
        return OAuth2TokenValidatorResult.success();
      }

      String authorizedParty = jwt.getClaimAsString("azp");
      List<String> audience = jwt.getAudience();
      if (expectedAuthorizedParty.equals(authorizedParty)
          || (audience != null && audience.contains(expectedAuthorizedParty))) {
        return OAuth2TokenValidatorResult.success();
      }

      return OAuth2TokenValidatorResult.failure(
          new OAuth2Error(
              "invalid_token",
              "The token is not intended for authorized party: " + expectedAuthorizedParty,
              null));
    };
  }
}
