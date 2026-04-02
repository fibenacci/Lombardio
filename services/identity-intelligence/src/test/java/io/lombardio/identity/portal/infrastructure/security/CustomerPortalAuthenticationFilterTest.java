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
package io.lombardio.identity.portal.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.lombardio.identity.config.CustomerPortalSessionProperties;
import io.lombardio.identity.portal.application.CustomerPortalService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class CustomerPortalAuthenticationFilterTest {

  private final CustomerPortalService customerPortalService = mock(CustomerPortalService.class);

  @AfterEach
  void clearContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void authenticatesCustomerPortalRequestFromSessionCookie() throws Exception {
    CustomerPortalAuthenticationFilter filter = newFilter();
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/customer-portal/me");
    request.setCookies(new jakarta.servlet.http.Cookie("lombardio_customer_portal_session", "portal-cookie"));
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);
    AuthenticatedCustomerPortalUser principal =
        new AuthenticatedCustomerPortalUser(
            "customer-1", "tenant-default", "Anna Example", "anna@example.test");
    when(customerPortalService.authenticate("portal-cookie")).thenReturn(principal);

    filter.doFilter(request, response, chain);

    verify(customerPortalService).authenticate("portal-cookie");
    verify(chain).doFilter(request, response);
  }

  @Test
  void ignoresBearerHeaderForCustomerPortalRequests() throws Exception {
    CustomerPortalAuthenticationFilter filter = newFilter();
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/customer-portal/me");
    request.addHeader("Authorization", "Bearer leaked-token");
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);

    filter.doFilter(request, response, chain);

    verify(customerPortalService).authenticate(null);
    verify(chain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void skipsNonPortalRequests() throws Exception {
    CustomerPortalAuthenticationFilter filter = newFilter();
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/customers/health");
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);

    filter.doFilter(request, response, chain);

    verifyNoInteractions(customerPortalService);
    verify(chain).doFilter(request, response);
  }

  private CustomerPortalAuthenticationFilter newFilter() {
    return new CustomerPortalAuthenticationFilter(
        customerPortalService,
        new CustomerPortalSessionProperties(
            "lombardio_customer_portal_session",
            "/",
            false,
            "Lax",
            2_592_000L,
            2_592_000L,
            3_600_000L));
  }
}
