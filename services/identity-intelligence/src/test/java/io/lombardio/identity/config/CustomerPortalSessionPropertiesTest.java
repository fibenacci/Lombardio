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
package io.lombardio.identity.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CustomerPortalSessionPropertiesTest {

  @Test
  void acceptsConsistentCookieAndSessionSettings() {
    assertDoesNotThrow(
        () ->
            new CustomerPortalSessionProperties(
                "cookie", "/", true, "Lax", 86_400L, 86_400L, 3_600_000L));
  }

  @Test
  void rejectsUnsupportedSameSiteValue() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new CustomerPortalSessionProperties(
                "cookie", "/", false, "Invalid", 86_400L, 86_400L, 3_600_000L));
  }

  @Test
  void rejectsCookieAgeThatOutlivesServerSession() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new CustomerPortalSessionProperties(
                "cookie", "/", false, "Lax", 86_401L, 86_400L, 3_600_000L));
  }
}
