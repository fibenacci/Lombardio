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

import io.lombardio.platform.iam.application.IdentityAdministration;
import io.lombardio.platform.iam.application.IdentityProviderUnavailableException;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class KeycloakAdminHealthIndicator implements HealthIndicator {

  private final IdentityAdministration identityAdministration;

  public KeycloakAdminHealthIndicator(IdentityAdministration identityAdministration) {
    this.identityAdministration = identityAdministration;
  }

  @Override
  public Health health() {
    try {
      return identityAdministration.canReachAdminApi()
          ? Health.up().withDetail("keycloakAdminApi", "reachable").build()
          : Health.unknown().withDetail("keycloakAdminApi", "unknown").build();
    } catch (IdentityProviderUnavailableException exception) {
      return Health.down(exception).withDetail("keycloakAdminApi", "unreachable").build();
    }
  }
}
