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
import io.lombardio.platform.auth.application.OperatorSessionUserView;
import io.lombardio.platform.auth.application.StoredOperatorSession;
import io.lombardio.platform.auth.application.StoredOperatorSessionService;
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
  private final StoredOperatorSessionService storedOperatorSessionService;
  private final OperatorSessionProperties sessionProperties;

  public OperatorAuthController(
      OperatorAuthService operatorAuthService,
      StoredOperatorSessionService storedOperatorSessionService,
      OperatorSessionProperties sessionProperties) {
    this.operatorAuthService = operatorAuthService;
    this.storedOperatorSessionService = storedOperatorSessionService;
    this.sessionProperties = sessionProperties;
  }

  @PostMapping("/login")
  public OperatorSessionResponse login(
      @Valid @RequestBody OperatorLoginRequest request, HttpServletResponse response) {
    OperatorSession session = operatorAuthService.login(request.email(), request.password());
    StoredOperatorSession storedSession = storedOperatorSessionService.createSession(session);
    writeSessionCookie(response, storedSession.sessionId());
    return toCookieBackedSessionResponse(storedSession.user());
  }

  @PostMapping("/refresh")
  public ResponseEntity<OperatorSessionResponse> refresh(
      HttpServletRequest request, HttpServletResponse response) {
    String sessionId = readSessionId(request);
    if (sessionId == null || sessionId.isBlank()) {
      return ResponseEntity.noContent().build();
    }

    return storedOperatorSessionService
        .refreshSession(sessionId)
        .map(
            session -> {
              writeSessionCookie(response, session.sessionId());
              return ResponseEntity.ok(toCookieBackedSessionResponse(session.user()));
            })
        .orElseGet(() -> ResponseEntity.noContent().build());
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
    storedOperatorSessionService.logout(readSessionId(request));
    clearSessionCookie(response);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/me")
  public OperatorSessionUserResponse me(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
    return OperatorSessionUserResponse.fromView(
        OperatorSessionUserView.fromAuthenticatedUser(authenticatedUser));
  }

  private OperatorSessionResponse toCookieBackedSessionResponse(OperatorSessionUserView user) {
    return new OperatorSessionResponse("AUTHENTICATED", OperatorSessionUserResponse.fromView(user));
  }

  private void writeSessionCookie(HttpServletResponse response, String sessionId) {
    response.addHeader(
        HttpHeaders.SET_COOKIE,
        buildSessionCookie(sessionId, sessionProperties.cookieMaxAgeSeconds()).toString());
  }

  private void clearSessionCookie(HttpServletResponse response) {
    response.addHeader(HttpHeaders.SET_COOKIE, buildSessionCookie("", 0).toString());
  }

  private ResponseCookie buildSessionCookie(String value, long maxAgeSeconds) {
    return ResponseCookie.from(sessionProperties.cookieName(), value)
        .httpOnly(true)
        .secure(sessionProperties.cookieSecure())
        .sameSite(sessionProperties.cookieSameSite())
        .path(sessionProperties.cookiePath())
        .maxAge(maxAgeSeconds)
        .build();
  }

  private String readSessionId(HttpServletRequest request) {
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
