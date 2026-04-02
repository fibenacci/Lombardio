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
package io.lombardio.platform.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.util.StringUtils;

public class CookieOrHeaderBearerTokenResolver implements BearerTokenResolver {

  public static final String DEFAULT_ACCESS_COOKIE_NAME = "lombardio_operator_access";

  private final DefaultBearerTokenResolver headerResolver = new DefaultBearerTokenResolver();
  private final String accessCookieName;

  public CookieOrHeaderBearerTokenResolver(String accessCookieName) {
    this.accessCookieName =
        StringUtils.hasText(accessCookieName) ? accessCookieName : DEFAULT_ACCESS_COOKIE_NAME;
  }

  @Override
  public String resolve(HttpServletRequest request) {
    String headerToken = headerResolver.resolve(request);
    if (StringUtils.hasText(headerToken)) {
      return headerToken;
    }

    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }

    for (Cookie cookie : cookies) {
      if (accessCookieName.equals(cookie.getName()) && StringUtils.hasText(cookie.getValue())) {
        return cookie.getValue();
      }
    }

    return null;
  }
}
