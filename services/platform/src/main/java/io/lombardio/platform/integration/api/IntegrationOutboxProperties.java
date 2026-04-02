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
package io.lombardio.platform.integration.api;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.outbox")
public record IntegrationOutboxProperties(String accessToken) {

  public IntegrationOutboxProperties {
    if (accessToken == null
        || accessToken.isBlank()
        || "change-me-platform-outbox-token".equals(accessToken)
        || "internal-secret-token".equals(accessToken)) {
      throw new IllegalArgumentException(
          "integration.outbox.access-token must be configured with a secure value");
    }
  }
}
