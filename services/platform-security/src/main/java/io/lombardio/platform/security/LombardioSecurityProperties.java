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

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lombardio.security")
public record LombardioSecurityProperties(
    String jwkSetUri,
    String operatorClientId,
    String operatorAccessCookieName) {

  public LombardioSecurityProperties {
    if (operatorAccessCookieName == null || operatorAccessCookieName.isBlank()) {
      operatorAccessCookieName = "lombardio_operator_access";
    }
  }
}
