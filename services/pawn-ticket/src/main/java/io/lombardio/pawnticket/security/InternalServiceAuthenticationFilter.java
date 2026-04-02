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
package io.lombardio.pawnticket.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class InternalServiceAuthenticationFilter extends OncePerRequestFilter {

  public static final String INTERNAL_AUTH_HEADER = "X-Internal-Service-Token";
  public static final String INTERNAL_SERVICE_AUTHORITY = "internal.service";

  private final String internalServiceToken;

  public InternalServiceAuthenticationFilter(@Value("${internal.service-token}") String internalServiceToken) {
    this.internalServiceToken = requireSecureToken(internalServiceToken);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path == null || !path.startsWith("/api/internal/");
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String requestToken = request.getHeader(INTERNAL_AUTH_HEADER);
    if (internalServiceToken.equals(requestToken)) {
      SecurityContextHolder.getContext()
          .setAuthentication(
              new UsernamePasswordAuthenticationToken(
                  "internal-service",
                  null,
                  List.of(new SimpleGrantedAuthority(INTERNAL_SERVICE_AUTHORITY))));
    }

    filterChain.doFilter(request, response);
  }

  private static String requireSecureToken(String token) {
    if (token == null
        || token.isBlank()
        || "REPLACE_WITH_SECURE_TOKEN".equals(token)
        || "dev-internal-token".equals(token)) {
      throw new IllegalStateException("internal.service-token must be configured with a secure value");
    }
    return token;
  }
}
