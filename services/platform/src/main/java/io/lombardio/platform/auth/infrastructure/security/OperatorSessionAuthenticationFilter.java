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
package io.lombardio.platform.auth.infrastructure.security;

import io.lombardio.platform.auth.application.StoredOperatorAuthentication;
import io.lombardio.platform.auth.application.StoredOperatorSessionService;
import io.lombardio.platform.config.OperatorSessionProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class OperatorSessionAuthenticationFilter extends OncePerRequestFilter {

  private final StoredOperatorSessionService storedOperatorSessionService;
  private final OperatorSessionProperties properties;

  public OperatorSessionAuthenticationFilter(
      StoredOperatorSessionService storedOperatorSessionService,
      OperatorSessionProperties properties) {
    this.storedOperatorSessionService = storedOperatorSessionService;
    this.properties = properties;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    if (SecurityContextHolder.getContext().getAuthentication() == null) {
      readSessionId(request)
          .flatMap(storedOperatorSessionService::authenticate)
          .ifPresent(this::authenticate);
    }
    filterChain.doFilter(request, response);
  }

  private void authenticate(StoredOperatorAuthentication session) {
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(
            session.operator(),
            session.accessToken(),
            session.operator().permissions().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet()));
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  private java.util.Optional<String> readSessionId(HttpServletRequest request) {
    String headerId = request.getHeader("X-Operator-Session-Id");
    if (headerId != null && !headerId.isBlank()) {
      return java.util.Optional.of(headerId);
    }

    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return java.util.Optional.empty();
    }

    return java.util.Arrays.stream(cookies)
        .filter(cookie -> properties.cookieName().equals(cookie.getName()))
        .map(Cookie::getValue)
        .filter(value -> value != null && !value.isBlank())
        .findFirst();
  }
}
