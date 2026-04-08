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

import io.lombardio.platform.auth.application.Operator;
import io.lombardio.platform.auth.application.OperatorAuthService;
import io.lombardio.platform.auth.application.OperatorIdentityTokens;
import io.lombardio.platform.auth.application.OperatorSession;
import io.lombardio.platform.auth.application.OperatorSessionUserView;
import io.lombardio.platform.auth.application.StoredOperatorSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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

  public OperatorAuthController(
      OperatorAuthService operatorAuthService,
      StoredOperatorSessionService storedOperatorSessionService) {
    this.operatorAuthService = operatorAuthService;
    this.storedOperatorSessionService = storedOperatorSessionService;
  }

  @PostMapping("/login")
  public OperatorSessionResponse login(@Valid @RequestBody OperatorLoginRequest request) {
    OperatorIdentityTokens tokens = operatorAuthService.login(request.email(), request.password());
    OperatorSession session = storedOperatorSessionService.createSession(tokens);
    return toSessionResponse(session.sessionId(), session.user());
  }

  @PostMapping("/refresh")
  public ResponseEntity<OperatorSessionResponse> refresh(HttpServletRequest request) {
    String sessionId = request.getHeader("X-Operator-Session-Id");
    if (sessionId == null || sessionId.isBlank()) {
      return ResponseEntity.noContent().build();
    }

    return storedOperatorSessionService
        .refreshSession(sessionId)
        .map(session -> ResponseEntity.ok(toSessionResponse(session.sessionId(), session.user())))
        .orElseGet(() -> ResponseEntity.noContent().build());
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletRequest request) {
    String sessionId = request.getHeader("X-Operator-Session-Id");
    if (sessionId != null && !sessionId.isBlank()) {
      storedOperatorSessionService.logout(sessionId);
    }
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/me")
  public OperatorSessionUserView me(@AuthenticationPrincipal Operator operator) {
    if (operator == null) {
      throw new io.lombardio.platform.security.UnauthorizedException("Not authenticated");
    }
    return OperatorSessionUserView.fromOperator(operator);
  }

  private OperatorSessionResponse toSessionResponse(
      String sessionId, OperatorSessionUserView user) {
    return new OperatorSessionResponse("AUTHENTICATED", sessionId, user);
  }
}
