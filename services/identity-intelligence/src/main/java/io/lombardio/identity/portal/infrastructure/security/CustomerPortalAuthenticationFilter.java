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
package io.lombardio.identity.portal.infrastructure.security;

import io.lombardio.identity.portal.application.CustomerPortalService;
import io.lombardio.identity.config.CustomerPortalSessionProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class CustomerPortalAuthenticationFilter extends OncePerRequestFilter {

  private final CustomerPortalService customerPortalService;
  private final CustomerPortalSessionProperties sessionProperties;

  public CustomerPortalAuthenticationFilter(
      CustomerPortalService customerPortalService, CustomerPortalSessionProperties sessionProperties) {
    this.customerPortalService = customerPortalService;
    this.sessionProperties = sessionProperties;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path == null || !path.startsWith("/api/v1/customer-portal/");
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String tokenValue = readSessionToken(request);
    AuthenticatedCustomerPortalUser principal = customerPortalService.authenticate(tokenValue);
    if (principal != null) {
      SecurityContextHolder.getContext()
          .setAuthentication(
              new UsernamePasswordAuthenticationToken(principal, tokenValue, List.of()));
    }

    filterChain.doFilter(request, response);
  }

  private String readSessionToken(HttpServletRequest request) {
    if (request.getCookies() == null) {
      return null;
    }

    for (Cookie cookie : request.getCookies()) {
      if (sessionProperties.cookieName().equals(cookie.getName())) {
        return cookie.getValue();
      }
    }
    return null;
  }
}
