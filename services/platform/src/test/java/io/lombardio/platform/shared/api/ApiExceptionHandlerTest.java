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
package io.lombardio.platform.shared.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.lombardio.platform.iam.application.IdentityProviderUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

class ApiExceptionHandlerTest {

  private final ApiExceptionHandler handler = new ApiExceptionHandler();

  @Test
  void mapsIdentityProviderFailuresToBadGateway() {
    var response =
        handler.handleIdentityProviderUnavailable(
            new IdentityProviderUnavailableException("Keycloak admin access failed", null),
            new MockHttpServletRequest());

    assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
    assertEquals("identity_provider_unavailable", response.getBody().code());
  }
}
