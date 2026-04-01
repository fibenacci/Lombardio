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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

public class KeycloakJwtAuthenticationConverter
    implements Converter<Jwt, AbstractAuthenticationToken> {

  @Override
  public AbstractAuthenticationToken convert(Jwt jwt) {
    Collection<GrantedAuthority> authorities = extractAuthorities(jwt);

    AuthenticatedUser principal =
        new AuthenticatedUser(
            jwt.getSubject(),
            jwt.getClaimAsString("actorUserId") != null
                ? jwt.getClaimAsString("actorUserId")
                : jwt.getSubject(),
            jwt.getClaimAsString("tenantId"),
            Boolean.TRUE.equals(jwt.getClaimAsBoolean("impersonating")),
            jwt.getClaimAsString("email"),
            jwt.getClaimAsString("name"), // Keycloak default for displayName
            authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));

    return new UsernamePasswordAuthenticationToken(principal, jwt, authorities) {
      @Override
      public void eraseCredentials() {
        // Downstream services still need the bearer token to call other internal APIs
        // on behalf of the current user, for example tenant-scoped feature lookups.
      }
    };
  }

  private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
    Map<String, Object> realmAccess = jwt.getClaim("realm_access");
    if (realmAccess == null || !realmAccess.containsKey("roles")) {
      return List.of();
    }

    List<String> roles = (List<String>) realmAccess.get("roles");
    return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
  }
}
