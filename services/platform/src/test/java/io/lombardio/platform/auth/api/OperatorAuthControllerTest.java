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
import org.springframework.mock.web.MockHttpServletResponse;

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
  void loginSetsHttpOnlySessionCookie() {
    OperatorSessionUserResponse user =
        new OperatorSessionUserResponse(
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

    MockHttpServletResponse response = new MockHttpServletResponse();
    OperatorSessionResponse body =
        controller.login(new OperatorLoginRequest("admin@lombardio.local", "admin"), response);

    assertEquals("AUTHENTICATED", body.status());
    String[] cookies = response.getHeaders("Set-Cookie").toArray(String[]::new);
    assertEquals(1, cookies.length);
    assertEquals(true, cookies[0].contains("lombardio_operator_session=session-id"));
    assertEquals(true, cookies[0].contains("HttpOnly"));
    assertEquals(true, cookies[0].contains("SameSite=Lax"));
  }

  @Test
  void refreshReturnsNoContentWithoutSessionCookie() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    ResponseEntity<OperatorSessionResponse> entity = controller.refresh(request, response);

    assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());
  }

  @Test
  void refreshReturnsSessionWhenCookieExists() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(new jakarta.servlet.http.Cookie("lombardio_operator_session", "session-id"));
    MockHttpServletResponse response = new MockHttpServletResponse();
    OperatorSessionUserResponse user =
        new OperatorSessionUserResponse(
            "user-1",
            "user-1",
            "tenant-default",
            "admin@lombardio.local",
            "Admin",
            false,
            List.of("users.read"),
            List.of("users.read"));

    when(storedOperatorSessionService.refreshSession("session-id"))
        .thenReturn(java.util.Optional.of(new StoredOperatorSession("session-id", user)));

    ResponseEntity<OperatorSessionResponse> entity = controller.refresh(request, response);

    assertEquals(HttpStatus.OK, entity.getStatusCode());
    assertEquals("AUTHENTICATED", entity.getBody().status());
    assertEquals(1, response.getHeaders("Set-Cookie").size());
    assertEquals(
        true,
        response.getHeaders("Set-Cookie").getFirst().contains("lombardio_operator_session=session-id"));
  }

  @Test
  void logoutClearsSessionCookie() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(new jakarta.servlet.http.Cookie("lombardio_operator_session", "session-id"));
    MockHttpServletResponse response = new MockHttpServletResponse();

    ResponseEntity<Void> entity = controller.logout(request, response);

    verify(storedOperatorSessionService).logout("session-id");
    assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());
    String[] cookies = response.getHeaders("Set-Cookie").toArray(String[]::new);
    assertEquals(1, cookies.length);
    assertEquals(true, cookies[0].contains("lombardio_operator_session="));
    assertEquals(true, cookies[0].contains("Max-Age=0"));
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
