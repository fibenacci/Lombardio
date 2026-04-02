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
package io.lombardio.identity.portal.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.lombardio.identity.config.CustomerPortalSessionProperties;
import io.lombardio.identity.portal.application.CustomerPortalService;
import io.lombardio.identity.portal.infrastructure.security.AuthenticatedCustomerPortalUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CustomerPortalControllerTest {

  private final CustomerPortalService customerPortalService = mock(CustomerPortalService.class);
  private CustomerPortalController controller;

  @BeforeEach
  void setUp() {
    controller =
        new CustomerPortalController(
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

  @Test
  void loginSetsSessionCookie() {
    CustomerPortalLoginResponse session =
        new CustomerPortalLoginResponse(
            "portal-token",
            new CustomerPortalMeResponse("customer-1", "tenant-default", "Anna Example", "anna@example.test", "ACTIVE"));
    when(customerPortalService.login(new CustomerPortalLoginRequest("anna@example.test", "secret123")))
        .thenReturn(session);

    MockHttpServletResponse response = new MockHttpServletResponse();
    CustomerPortalLoginResponse body =
        controller.login(new CustomerPortalLoginRequest("anna@example.test", "secret123"), response);

    assertEquals(null, body.accessToken());
    assertEquals(true, response.getHeader("Set-Cookie").contains("lombardio_customer_portal_session=portal-token"));
  }

  @Test
  void refreshReturnsNoContentWithoutCookie() {
    ResponseEntity<CustomerPortalLoginResponse> response =
        controller.refresh(new MockHttpServletRequest(), new MockHttpServletResponse());

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
  }

  @Test
  void logoutClearsCookieAndDeletesSession() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(new jakarta.servlet.http.Cookie("lombardio_customer_portal_session", "portal-token"));
    MockHttpServletResponse response = new MockHttpServletResponse();

    ResponseEntity<Void> entity = controller.logout(request, response);

    verify(customerPortalService).logout("portal-token");
    assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());
    assertEquals(true, response.getHeader("Set-Cookie").contains("Max-Age=0"));
  }

  @Test
  void meReturnsCurrentCustomer() {
    when(customerPortalService.currentCustomer(
            new AuthenticatedCustomerPortalUser(
                "customer-1", "tenant-default", "Anna Example", "anna@example.test")))
        .thenReturn(
            new CustomerPortalMeResponse(
                "customer-1",
                "tenant-default",
                "Anna Example",
                "anna@example.test",
                "ACTIVE"));

    CustomerPortalMeResponse response =
        controller.me(new AuthenticatedCustomerPortalUser("customer-1", "tenant-default", "Anna Example", "anna@example.test"));

    assertEquals("customer-1", response.customerId());
    assertEquals("anna@example.test", response.email());
  }
}
