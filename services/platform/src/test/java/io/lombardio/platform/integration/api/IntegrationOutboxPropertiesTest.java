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
package io.lombardio.platform.integration.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class IntegrationOutboxPropertiesTest {

  @Test
  void acceptsSecureAccessToken() {
    IntegrationOutboxProperties properties =
        new IntegrationOutboxProperties("secure-platform-outbox-token");

    assertEquals("secure-platform-outbox-token", properties.accessToken());
  }

  @Test
  void rejectsPlaceholderAccessToken() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new IntegrationOutboxProperties("change-me-platform-outbox-token"));
  }
}
