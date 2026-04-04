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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

class AuthenticatedUserTest {

  @AfterEach
  void clearContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void returnsJwtTokenForAuthenticatedUserPrincipal() {
    AuthenticatedUser principal =
        new AuthenticatedUser(
            "user-1", "user-1", "tenant-default", false, "u@example.test", "User", List.of());
    Jwt jwt =
        Jwt.withTokenValue("jwt-token")
            .header("alg", "none")
            .subject("user-1")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(300))
            .build();
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(principal, jwt, List.of()));

    assertEquals("jwt-token", AuthenticatedUser.currentAccessToken().orElseThrow());
  }

  @Test
  void ignoresStringCredentialsFromNonAuthenticatedUserPrincipal() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken("internal-service", "secret", List.of()));

    assertTrue(AuthenticatedUser.currentAccessToken().isEmpty());
  }
}
