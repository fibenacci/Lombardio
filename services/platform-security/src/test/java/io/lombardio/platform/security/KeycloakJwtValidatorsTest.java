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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class KeycloakJwtValidatorsTest {

  @Test
  void acceptsTrustedIssuerAndAuthorizedParty() {
    Jwt jwt =
        Jwt.withTokenValue("token")
            .header("alg", "none")
            .issuer("http://localhost:8080/realms/lombardio")
            .audience(List.of("account", "lombardio-app"))
            .claim("azp", "lombardio-app")
            .subject("user-1")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(300))
            .build();

    assertFalse(
        KeycloakJwtValidators.operatorAccessTokenValidator("lombardio-app")
            .validate(jwt)
            .hasErrors());
  }

  @Test
  void rejectsUnexpectedAuthorizedParty() {
    Jwt jwt =
        Jwt.withTokenValue("token")
            .header("alg", "none")
            .issuer("http://localhost:8080/realms/lombardio")
            .audience(List.of("account", "other-client"))
            .claim("azp", "other-client")
            .subject("user-1")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(300))
            .build();

    assertTrue(
        KeycloakJwtValidators.operatorAccessTokenValidator("lombardio-app")
            .validate(jwt)
            .hasErrors());
  }
}
