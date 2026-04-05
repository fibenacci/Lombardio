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
package io.lombardio.loanorigination.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cors")
public record AppCorsProperties(
    List<String> allowedOrigins,
    List<String> allowedMethods,
    List<String> allowedHeaders,
    List<String> exposedHeaders,
    Long maxAgeSeconds) {

  public AppCorsProperties {
    allowedOrigins = List.copyOf(allowedOrigins == null ? List.of() : allowedOrigins);
    allowedMethods = List.copyOf(allowedMethods == null ? List.of() : allowedMethods);
    allowedHeaders = List.copyOf(allowedHeaders == null ? List.of() : allowedHeaders);
    exposedHeaders = List.copyOf(exposedHeaders == null ? List.of() : exposedHeaders);
  }

  @Override
  public List<String> allowedOrigins() {
    return List.copyOf(allowedOrigins);
  }

  @Override
  public List<String> allowedMethods() {
    return List.copyOf(allowedMethods);
  }

  @Override
  public List<String> allowedHeaders() {
    return List.copyOf(allowedHeaders);
  }

  @Override
  public List<String> exposedHeaders() {
    return List.copyOf(exposedHeaders);
  }
}
