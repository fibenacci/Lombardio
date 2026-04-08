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
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public record AuthenticatedUser(
    String userId,
    String actorUserId,
    String tenantId,
    boolean impersonating,
    String email,
    String displayName,
    List<String> permissions) {

  public AuthenticatedUser {
    permissions = List.copyOf(permissions != null ? permissions : List.of());
  }

  public boolean hasPermission(String permission) {
    return permissions.contains(permission);
  }

  public static Optional<String> currentAccessToken() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      return Optional.empty();
    }
    if (authentication.getCredentials() instanceof String token) {
      return Optional.of(token);
    }
    if (authentication.getCredentials()
        instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
      return Optional.of(jwt.getTokenValue());
    }
    return Optional.empty();
  }
}
