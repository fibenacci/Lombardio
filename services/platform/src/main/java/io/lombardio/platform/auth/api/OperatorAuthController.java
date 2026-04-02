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
package io.lombardio.platform.auth.api;

import io.lombardio.platform.auth.application.OperatorAuthService;
import io.lombardio.platform.auth.application.OperatorSession;
import io.lombardio.platform.config.OperatorSessionProperties;
import io.lombardio.platform.security.AuthenticatedUser;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/platform/auth")
public class OperatorAuthController {

  private final OperatorAuthService operatorAuthService;
  private final OperatorSessionProperties sessionProperties;

  public OperatorAuthController(
      OperatorAuthService operatorAuthService, OperatorSessionProperties sessionProperties) {
    this.operatorAuthService = operatorAuthService;
    this.sessionProperties = sessionProperties;
  }

  @PostMapping("/login")
  public OperatorSessionResponse login(
      @Valid @RequestBody OperatorLoginRequest request, HttpServletResponse response) {
    OperatorSession session = operatorAuthService.login(request.email(), request.password());
    writeRefreshCookie(response, session.refreshToken());
    return new OperatorSessionResponse("AUTHENTICATED", session.accessToken(), session.user());
  }

  @PostMapping("/refresh")
  public ResponseEntity<OperatorSessionResponse> refresh(
      HttpServletRequest request,
      HttpServletResponse response) {
    String refreshToken = readRefreshToken(request);
    if (refreshToken == null || refreshToken.isBlank()) {
      return ResponseEntity.noContent().build();
    }

    OperatorSession session = operatorAuthService.refresh(refreshToken);
    writeRefreshCookie(response, session.refreshToken());
    return ResponseEntity.ok(
        new OperatorSessionResponse("AUTHENTICATED", session.accessToken(), session.user()));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(
      HttpServletRequest request,
      HttpServletResponse response) {
    String refreshToken = readRefreshToken(request);
    operatorAuthService.logout(refreshToken);
    clearRefreshCookie(response);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/me")
  public OperatorSessionUserResponse me(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
    return OperatorSessionUserResponse.fromAuthenticatedUser(authenticatedUser);
  }

  private void writeRefreshCookie(HttpServletResponse response, String refreshToken) {
    response.addHeader(
        HttpHeaders.SET_COOKIE,
        buildRefreshCookie(refreshToken, sessionProperties.refreshCookieMaxAgeSeconds()).toString());
  }

  private void clearRefreshCookie(HttpServletResponse response) {
    response.addHeader(HttpHeaders.SET_COOKIE, buildRefreshCookie("", 0).toString());
  }

  private ResponseCookie buildRefreshCookie(String value, long maxAgeSeconds) {
    return ResponseCookie.from(sessionProperties.refreshCookieName(), value)
        .httpOnly(true)
        .secure(sessionProperties.refreshCookieSecure())
        .sameSite(sessionProperties.refreshCookieSameSite())
        .path(sessionProperties.refreshCookiePath())
        .maxAge(maxAgeSeconds)
        .build();
  }

  private String readRefreshToken(HttpServletRequest request) {
    if (request.getCookies() == null) {
      return null;
    }

    for (Cookie cookie : request.getCookies()) {
      if (sessionProperties.refreshCookieName().equals(cookie.getName())) {
        return cookie.getValue();
      }
    }
    return null;
  }
}
