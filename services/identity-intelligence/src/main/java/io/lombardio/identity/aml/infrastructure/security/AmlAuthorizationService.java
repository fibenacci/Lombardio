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
package io.lombardio.identity.aml.infrastructure.security;

import io.lombardio.platform.security.AuthenticatedUser;
import io.lombardio.platform.security.BaseAuthorizationService;
import org.springframework.stereotype.Service;

@Service
public class AmlAuthorizationService extends BaseAuthorizationService {

  public void requireRead(AuthenticatedUser user, String tenantId) {
    requireTenantAccess(user, tenantId, "aml.read", "platform.tenants.read");
  }

  public void requireWrite(AuthenticatedUser user, String tenantId) {
    requireTenantAccess(user, tenantId, "aml.write", "platform.tenants.write");
  }
}
