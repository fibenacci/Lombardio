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
  private OperatorAuthController controller;

  @BeforeEach
  void setUp() {
    controller =
        new OperatorAuthController(
            operatorAuthService,
            new OperatorSessionProperties(
                "lombardio_operator_refresh", "/", false, "Lax", 2_592_000L));
  }

  @Test
  void loginSetsHttpOnlyRefreshCookie() {
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
    when(operatorAuthService.login("admin@lombardio.local", "admin"))
        .thenReturn(new OperatorSession("access-token", "refresh-token", user));

    MockHttpServletResponse response = new MockHttpServletResponse();
    OperatorSessionResponse body =
        controller.login(new OperatorLoginRequest("admin@lombardio.local", "admin"), response);

    assertEquals("AUTHENTICATED", body.status());
    assertEquals("access-token", body.accessToken());
    String cookie = response.getHeader("Set-Cookie");
    assertEquals(true, cookie.contains("lombardio_operator_refresh=refresh-token"));
    assertEquals(true, cookie.contains("HttpOnly"));
    assertEquals(true, cookie.contains("SameSite=Lax"));
  }

  @Test
  void refreshReturnsNoContentWithoutRefreshCookie() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    ResponseEntity<OperatorSessionResponse> entity = controller.refresh(request, response);

    assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());
  }

  @Test
  void logoutClearsRefreshCookie() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(new jakarta.servlet.http.Cookie("lombardio_operator_refresh", "refresh-token"));
    MockHttpServletResponse response = new MockHttpServletResponse();

    ResponseEntity<Void> entity = controller.logout(request, response);

    verify(operatorAuthService).logout("refresh-token");
    assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());
    assertEquals(true, response.getHeader("Set-Cookie").contains("Max-Age=0"));
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
