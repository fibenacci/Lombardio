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
package io.lombardio.platform.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class OperatorSessionPropertiesTest {

  @Test
  void acceptsSupportedSameSiteValues() {
    assertDoesNotThrow(
        () ->
            new OperatorSessionProperties(
                "session", "/", false, "Lax", 900, "0123456789abcdef"));
    assertDoesNotThrow(
        () ->
            new OperatorSessionProperties(
                "session", "/", true, "Strict", 900, "0123456789abcdef"));
    assertDoesNotThrow(
        () ->
            new OperatorSessionProperties(
                "session", "/", true, "None", 900, "0123456789abcdef"));
  }

  @Test
  void rejectsUnsupportedSameSiteValue() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new OperatorSessionProperties("session", "/", false, "lax", 900, "0123456789abcdef"));
  }

  @Test
  void rejectsNonPositiveCookieAge() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new OperatorSessionProperties("session", "/", false, "Lax", 0, "0123456789abcdef"));
  }

  @Test
  void rejectsShortEncryptionKey() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new OperatorSessionProperties("session", "/", false, "Lax", 900, "too-short"));
  }
}
