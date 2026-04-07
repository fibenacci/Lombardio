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

import io.lombardio.platform.auth.application.OperatorAuthService;
import io.lombardio.platform.auth.application.OperatorSession;
import io.lombardio.platform.auth.application.OperatorSessionUserView;
import io.lombardio.platform.auth.application.StoredOperatorSession;
import io.lombardio.platform.auth.application.StoredOperatorSessionService;
import io.lombardio.platform.config.OperatorSessionProperties;
import io.lombardio.platform.security.AuthenticatedUser;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

class OperatorAuthControllerTest {

  private final OperatorAuthService operatorAuthService = mock(OperatorAuthService.class);
  private final StoredOperatorSessionService storedOperatorSessionService =
      mock(StoredOperatorSessionService.class);
  private OperatorAuthController controller;

  @BeforeEach
  void setUp() {
    controller =
        new OperatorAuthController(
            operatorAuthService,
            storedOperatorSessionService,
            new OperatorSessionProperties(
                "lombardio_operator_session", "/", false, "Lax", 2_592_000L, "0123456789abcdef"));
  }

  @Test
  void loginReturnsSessionInResponseBody() {
    OperatorSessionUserView user =
        new OperatorSessionUserView(
            "user-1",
            "user-1",
            "tenant-default",
            "admin@lombardio.local",
            "Admin",
            false,
            List.of("users.read"),
            List.of("users.read"));
    OperatorSession keycloakSession = new OperatorSession("access-token", "refresh-token", user);
    when(operatorAuthService.login("admin@lombardio.local", "admin")).thenReturn(keycloakSession);
    when(storedOperatorSessionService.createSession(keycloakSession))
        .thenReturn(new StoredOperatorSession("session-id", user));

    OperatorSessionResponse body =
        controller.login(new OperatorLoginRequest("admin@lombardio.local", "admin"));

    assertEquals("AUTHENTICATED", body.status());
    assertEquals("session-id", body.sessionId());
    assertEquals("user-1", body.user().id());
  }

  @Test
  void refreshReturnsNoContentWithoutHeader() {
    MockHttpServletRequest request = new MockHttpServletRequest();

    ResponseEntity<OperatorSessionResponse> entity = controller.refresh(request);

    assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());
  }

  @Test
  void refreshReturnsSessionWhenHeaderExists() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("X-Operator-Session-Id", "session-id");
    OperatorSessionUserView user =
        new OperatorSessionUserView(
            "user-1",
            "user-1",
            "tenant-default",
            "admin@lombardio.local",
            "Admin",
            false,
            List.of("users.read"),
            List.of("users.read"));

    when(storedOperatorSessionService.refreshSession("session-id"))
        .thenReturn(java.util.Optional.of(new StoredOperatorSession("new-session-id", user)));

    ResponseEntity<OperatorSessionResponse> entity = controller.refresh(request);

    assertEquals(HttpStatus.OK, entity.getStatusCode());
    assertEquals("AUTHENTICATED", entity.getBody().status());
    assertEquals("new-session-id", entity.getBody().sessionId());
  }

  @Test
  void logoutCallsServiceWithHeader() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("X-Operator-Session-Id", "session-id");

    ResponseEntity<Void> entity = controller.logout(request);

    verify(storedOperatorSessionService).logout("session-id");
    assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());
  }

  @Test
  void meReturnsAuthenticatedUserProfile() {
    AuthenticatedUser user =
        new AuthenticatedUser(
            "user-1",
            "user-1",
            "tenant-default",
            false,
            "admin@lombardio.local",
            "Admin",
            List.of("users.read"));

    OperatorSessionUserResponse response = controller.me(user);

    assertEquals("user-1", response.id());
    assertEquals(List.of("users.read"), response.permissions());
  }
}
