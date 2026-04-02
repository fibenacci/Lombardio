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

import io.lombardio.identity.portal.infrastructure.persistence.CustomerPortalSessionEntity;

public record AuthenticatedCustomerPortalUser(
    String customerId,
    String tenantId,
    String displayName,
    String email,
    CustomerPortalSessionEntity session) {

  public AuthenticatedCustomerPortalUser(
      String customerId, String tenantId, String displayName, String email) {
    this(customerId, tenantId, displayName, email, null);
  }
}
