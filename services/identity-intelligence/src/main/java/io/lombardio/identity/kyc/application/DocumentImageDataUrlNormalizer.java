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
package io.lombardio.identity.kyc.application;

final class DocumentImageDataUrlNormalizer {

  private DocumentImageDataUrlNormalizer() {}

  static String normalize(String value) {
    if (value == null) {
      return null;
    }

    String normalizedValue = value.trim();
    if (normalizedValue.isEmpty() || normalizedValue.startsWith("data:")) {
      return normalizedValue;
    }

    return "data:" + inferMimeType(normalizedValue) + ";base64," + normalizedValue;
  }

  private static String inferMimeType(String base64Value) {
    if (base64Value.startsWith("/9j/")) {
      return "image/jpeg";
    }
    if (base64Value.startsWith("iVBOR")) {
      return "image/png";
    }
    if (base64Value.startsWith("R0lGOD")) {
      return "image/gif";
    }
    if (base64Value.startsWith("UklGR")) {
      return "image/webp";
    }
    return "image/png";
  }
}
