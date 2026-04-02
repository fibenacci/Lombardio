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

import io.lombardio.platform.config.OperatorBffProperties;
import java.net.URI;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
class OperatorBffTargetResolver {

  private final OperatorBffProperties properties;

  OperatorBffTargetResolver(OperatorBffProperties properties) {
    this.properties = properties;
  }

  URI resolve(String serviceKey, String downstreamPath, String query) {
    String baseUrl =
        properties
            .resolve(serviceKey)
            .orElseThrow(() -> new IllegalArgumentException("Unknown BFF service: " + serviceKey));
    return UriComponentsBuilder.fromUriString(baseUrl)
        .path(downstreamPath)
        .query(query)
        .build(true)
        .toUri();
  }
}
