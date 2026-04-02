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

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.operator-session")
public record OperatorSessionProperties(
    String cookieName,
    String cookiePath,
    boolean cookieSecure,
    String cookieSameSite,
    long cookieMaxAgeSeconds,
    String encryptionKey) {

  public OperatorSessionProperties {
    requireNonBlank(cookieName, "cookieName");
    requireNonBlank(cookiePath, "cookiePath");
    requireSupportedSameSite(cookieSameSite, "cookieSameSite");
    requirePositive(cookieMaxAgeSeconds, "cookieMaxAgeSeconds");
    requireReasonableEncryptionKey(encryptionKey);
  }

  private static void requireNonBlank(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " must not be blank");
    }
  }

  private static void requireSupportedSameSite(String value, String fieldName) {
    requireNonBlank(value, fieldName);
    if (!"Strict".equals(value) && !"Lax".equals(value) && !"None".equals(value)) {
      throw new IllegalArgumentException(fieldName + " must be one of Strict, Lax or None");
    }
  }

  private static void requirePositive(long value, String fieldName) {
    if (value <= 0) {
      throw new IllegalArgumentException(fieldName + " must be greater than 0");
    }
  }

  private static void requireReasonableEncryptionKey(String value) {
    requireNonBlank(value, "encryptionKey");
    if (value.length() < 16) {
      throw new IllegalArgumentException("encryptionKey must be at least 16 characters long");
    }
  }
}
