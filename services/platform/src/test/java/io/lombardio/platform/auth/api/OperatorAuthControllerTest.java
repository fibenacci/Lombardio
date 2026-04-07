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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.lombardio.platform.auth.application.Operator;
import io.lombardio.platform.auth.application.OperatorAuthService;
import io.lombardio.platform.auth.application.OperatorIdentityTokens;
import io.lombardio.platform.auth.application.OperatorSession;
import io.lombardio.platform.auth.application.OperatorSessionUserView;
import io.lombardio.platform.auth.application.StoredOperatorSessionService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

class OperatorAuthControllerTest {

  private final OperatorAuthService operatorAuthService = mock(OperatorAuthService.class);
  private final StoredOperatorSessionService storedOperatorSessionService =
      mock(StoredOperatorSessionService.class);
  private OperatorAuthController operatorAuthController;

  @BeforeEach
  void setUp() {
    operatorAuthController =
        new OperatorAuthController(operatorAuthService, storedOperatorSessionService);
  }

  @Test
  void loginReturnsSessionInResponseBody() {
    OperatorSessionUserView user =
        new OperatorSessionUserView(
            "user-1",
            "user-1",
            "tenant-1",
            "admin@lombardio.local",
            "Admin",
            false,
            List.of(),
            List.of());
    OperatorIdentityTokens tokens = new OperatorIdentityTokens("token-1", "refresh-1");

    when(operatorAuthService.login("admin@lombardio.local", "password")).thenReturn(tokens);
    when(storedOperatorSessionService.createSession(tokens))
        .thenReturn(new OperatorSession("session-1", user));

    OperatorSessionResponse response =
        operatorAuthController.login(new OperatorLoginRequest("admin@lombardio.local", "password"));

    assertEquals("session-1", response.sessionId());
    assertEquals("AUTHENTICATED", response.status());
    assertEquals(user, response.user());
  }

  @Test
  void refreshReturnsNoContentWithoutHeader() {
    MockHttpServletRequest request = new MockHttpServletRequest();

    ResponseEntity<OperatorSessionResponse> response = operatorAuthController.refresh(request);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
  }

  @Test
  void refreshReturnsSessionWhenHeaderExists() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("X-Operator-Session-Id", "session-1");
    OperatorSessionUserView user =
        new OperatorSessionUserView(
            "user-1",
            "user-1",
            "tenant-1",
            "admin@lombardio.local",
            "Admin",
            false,
            List.of(),
            List.of());
    when(storedOperatorSessionService.refreshSession("session-1"))
        .thenReturn(Optional.of(new OperatorSession("session-1", user)));

    ResponseEntity<OperatorSessionResponse> response = operatorAuthController.refresh(request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("session-1", response.getBody().sessionId());
  }

  @Test
  void logoutCallsServiceWithHeader() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("X-Operator-Session-Id", "session-1");

    operatorAuthController.logout(request);

    verify(storedOperatorSessionService).logout("session-1");
  }

  @Test
  void meReturnsAuthenticatedUserProfile() {
    Operator operator =
        new Operator(
            "user-1",
            "user-1",
            "tenant-1",
            false,
            "admin@lombardio.local",
            "Admin",
            List.of(),
            List.of("perm-1"));

    OperatorSessionUserView response = operatorAuthController.me(operator);

    assertEquals("user-1", response.id());
    assertEquals("Admin", response.displayName());
    assertEquals("admin@lombardio.local", response.email());
  }
}
