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
package io.lombardio.identity.portal.infrastructure.security;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.lombardio.identity.portal.infrastructure.persistence.CustomerPortalSessionEntity;

public record AuthenticatedCustomerPortalUser(
    String customerId,
    String tenantId,
    String displayName,
    String email,
    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP",
            justification = "Session context required by framework")
        CustomerPortalSessionEntity session) {

  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP2",
      justification = "Session context required by framework")
  public AuthenticatedCustomerPortalUser {}

  public AuthenticatedCustomerPortalUser(
      String customerId, String tenantId, String displayName, String email) {
    this(customerId, tenantId, displayName, email, null);
  }
}
