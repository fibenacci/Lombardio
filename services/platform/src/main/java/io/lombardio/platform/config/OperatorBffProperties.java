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

import java.util.Map;
import java.util.Optional;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.operator-bff")
public record OperatorBffProperties(Map<String, String> targets) {

  public OperatorBffProperties {
    targets = Map.copyOf(targets != null ? targets : Map.of());
  }

  public Optional<String> resolve(String serviceKey) {
    return Optional.ofNullable(targets.get(serviceKey));
  }
}
