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
package io.lombardio.platform.security;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lombardio.security.cors")
public record LombardioCorsProperties(
    List<String> allowedOrigins,
    List<String> allowedMethods,
    List<String> allowedHeaders,
    List<String> exposedHeaders,
    Long maxAgeSeconds) {

  public LombardioCorsProperties {
    allowedOrigins = List.copyOf(allowedOrigins != null ? allowedOrigins : List.of());
    allowedMethods = List.copyOf(allowedMethods != null ? allowedMethods : List.of());
    allowedHeaders = List.copyOf(allowedHeaders != null ? allowedHeaders : List.of());
    exposedHeaders = List.copyOf(exposedHeaders != null ? exposedHeaders : List.of());
  }
}
