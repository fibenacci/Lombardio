package io.lombardio.platform.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class CookieOrHeaderBearerTokenResolverTest {

  private final CookieOrHeaderBearerTokenResolver resolver =
      new CookieOrHeaderBearerTokenResolver("operator_access");

  @Test
  void prefersAuthorizationHeaderWhenPresent() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer header-token");
    request.setCookies(new Cookie("operator_access", "cookie-token"));

    assertEquals("header-token", resolver.resolve(request));
  }

  @Test
  void fallsBackToAccessCookie() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(new Cookie("operator_access", "cookie-token"));

    assertEquals("cookie-token", resolver.resolve(request));
  }

  @Test
  void returnsNullWhenNoTokenIsPresent() {
    MockHttpServletRequest request = new MockHttpServletRequest();

    assertNull(resolver.resolve(request));
  }
}
