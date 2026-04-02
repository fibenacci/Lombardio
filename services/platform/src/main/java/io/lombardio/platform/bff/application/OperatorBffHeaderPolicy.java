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
package io.lombardio.platform.bff.application;

import java.util.Locale;
import java.util.Set;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
class OperatorBffHeaderPolicy {

  private static final Set<String> EXCLUDED_REQUEST_HEADERS =
      Set.of("authorization", "cookie", "content-length", "host", "connection");
  private static final Set<String> EXCLUDED_RESPONSE_HEADERS =
      Set.of(
          "connection",
          "content-length",
          "keep-alive",
          "proxy-authenticate",
          "proxy-authorization",
          "set-cookie",
          "te",
          "trailer",
          "transfer-encoding",
          "upgrade");

  void applyForwardHeaders(
      HttpHeaders targetHeaders, HttpHeaders incomingHeaders, String accessToken) {
    incomingHeaders.forEach(
        (headerName, values) -> {
          String normalized = headerName.toLowerCase(Locale.ROOT);
          if (EXCLUDED_REQUEST_HEADERS.contains(normalized)) {
            return;
          }
          values.forEach(value -> targetHeaders.add(headerName, value));
        });
    targetHeaders.setBearerAuth(accessToken);
  }

  HttpHeaders sanitizeResponseHeaders(HttpHeaders sourceHeaders) {
    HttpHeaders targetHeaders = new HttpHeaders();
    sourceHeaders.forEach(
        (headerName, values) -> {
          String normalized = headerName.toLowerCase(Locale.ROOT);
          if (EXCLUDED_RESPONSE_HEADERS.contains(normalized)) {
            return;
          }
          values.forEach(value -> targetHeaders.add(headerName, value));
        });

    if (!targetHeaders.containsKey(HttpHeaders.CONTENT_TYPE)) {
      targetHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    }
    return targetHeaders;
  }
}
