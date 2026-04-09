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
package io.lombardio.platform.integration.security;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.lombardio.platform.auth.application.Operator;
import io.lombardio.platform.auth.application.StoredOperatorAuthentication;
import io.lombardio.platform.auth.application.StoredOperatorSessionService;
import jakarta.servlet.http.Cookie;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CsrfSecurityIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private StoredOperatorSessionService storedOperatorSessionService;

  @Test
  void shouldRejectPostWithoutCsrfTokenWhenUsingCookieAuth() throws Exception {
    // GIVEN: A valid session cookie exists
    String sessionId = "valid-session-id";
    Operator operator =
        new Operator(
            "user-1",
            "user-1",
            "tenant-1",
            false,
            "admin@lombardio.local",
            "Admin",
            List.of(),
            List.of("users.write"));

    when(storedOperatorSessionService.authenticate(sessionId))
        .thenReturn(Optional.of(new StoredOperatorAuthentication("access-token", operator)));

    // WHEN: A POST request is made with the session cookie but WITHOUT a CSRF token
    // THEN: It should be REJECTED (403 Forbidden) if CSRF protection is active.
    // CURRENT STATE: It will likely return 404 (because endpoint doesn't exist) or 200/400, but NOT
    // 403 CSRF.
    mockMvc
        .perform(
            post("/api/v1/platform/operator/tenants/tenant-1/users")
                .cookie(new Cookie("lombardio_operator_session", sessionId))
                .contentType("application/json")
                .content("{}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void shouldAllowPostWithBearerTokenWithoutCsrfToken() throws Exception {
    // GIVEN: A valid Bearer token (simulated by the filter chain)
    // We don't need to mock anything here if we assume the Bearer filter handles it,
    // but we want to ensure CSRF is NOT applied to Bearer requests.
    // In Spring Security, CSRF is usually applied to all requests unless explicitly ignored.
    // However, if we use a stateless session policy, we must be careful.

    mockMvc
        .perform(
            post("/api/v1/platform/operator/tenants/tenant-1/users")
                .header("Authorization", "Bearer valid-token")
                .contentType("application/json")
                .content("{}"))
        .andExpect(
            status().isUnauthorized()); // Unauthorized because JWT is invalid, but NOT 403 (CSRF)
  }
}
