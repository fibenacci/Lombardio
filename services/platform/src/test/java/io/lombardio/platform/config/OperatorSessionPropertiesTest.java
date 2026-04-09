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

  private static final String VALID_KEY_16 = "a-v3ry-s3cr3t-16";
  private static final String VALID_KEY_32 = "9p4w3v-v3ry-s3cr3t-t3st-k3y-32ch";

  @Test
  void acceptsSupportedSameSiteValues() {
    assertDoesNotThrow(
        () -> new OperatorSessionProperties("session", "/", false, "Lax", 900, VALID_KEY_32));
    assertDoesNotThrow(
        () -> new OperatorSessionProperties("session", "/", true, "Strict", 900, VALID_KEY_16));
    assertDoesNotThrow(
        () -> new OperatorSessionProperties("session", "/", true, "None", 900, VALID_KEY_32));
  }

  @Test
  void rejectsUnsupportedSameSiteValue() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new OperatorSessionProperties("session", "/", false, "lax", 900, VALID_KEY_32));
  }

  @Test
  void rejectsNonPositiveCookieAge() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new OperatorSessionProperties("session", "/", false, "Lax", 0, VALID_KEY_32));
  }

  @Test
  void rejectsInvalidEncryptionKeyLength() {
    // Too short
    assertThrows(
        IllegalArgumentException.class,
        () -> new OperatorSessionProperties("session", "/", false, "Lax", 900, "too-short"));
    // Middle length (not 16 or 32)
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new OperatorSessionProperties(
                "session", "/", false, "Lax", 900, "this-is-exactly-25-chars-"));
  }

  @Test
  void rejectsTrivialEncryptionKey() {
    // Sequential characters
    assertThrows(
        IllegalArgumentException.class,
        () -> new OperatorSessionProperties("session", "/", false, "Lax", 900, "0123456789abcdef"));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new OperatorSessionProperties(
                "session", "/", false, "Lax", 900, "abcdefghij1234567890abcdefghij12"));
    // Repeating characters
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new OperatorSessionProperties(
                "session", "/", false, "Lax", 900, "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
  }
}
