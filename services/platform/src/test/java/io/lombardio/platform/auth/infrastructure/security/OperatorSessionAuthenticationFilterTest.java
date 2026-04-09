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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.lombardio.platform.auth.application.Operator;
import io.lombardio.platform.auth.application.StoredOperatorAuthentication;
import io.lombardio.platform.auth.application.StoredOperatorSessionService;
import io.lombardio.platform.config.OperatorSessionProperties;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class OperatorSessionAuthenticationFilterTest {

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void authenticatesFromOpaqueSessionCookie() throws Exception {
    StoredOperatorSessionService storedOperatorSessionService =
        mock(StoredOperatorSessionService.class);
    OperatorSessionAuthenticationFilter filter =
        new OperatorSessionAuthenticationFilter(
            storedOperatorSessionService,
            new OperatorSessionProperties(
                "lombardio_operator_session",
                "/",
                false,
                "Lax",
                2_592_000L,
                "9p4w3v-v3ry-s3cr3t-t3st-k3y-32ch"));

    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/platform/auth/me");
    request.setCookies(new jakarta.servlet.http.Cookie("lombardio_operator_session", "session-id"));
    MockHttpServletResponse response = new MockHttpServletResponse();

    Operator operator =
        new Operator(
            "user-1",
            "user-1",
            "tenant-default",
            false,
            "admin@lombardio.local",
            "Admin",
            List.of(),
            List.of("users.read"));

    when(storedOperatorSessionService.authenticate("session-id"))
        .thenReturn(Optional.of(new StoredOperatorAuthentication("access-token", operator)));

    filter.doFilter(request, response, new MockFilterChain());

    assertEquals(operator, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    assertEquals(
        "access-token", SecurityContextHolder.getContext().getAuthentication().getCredentials());
  }
}
