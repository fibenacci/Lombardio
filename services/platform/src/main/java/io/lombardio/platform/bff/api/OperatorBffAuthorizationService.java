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
package io.lombardio.platform.bff.api;

import io.lombardio.platform.security.AuthenticatedUser;
import io.lombardio.platform.security.BaseAuthorizationService;
import org.springframework.stereotype.Service;

@Service
class OperatorBffAuthorizationService extends BaseAuthorizationService {

  /**
   * Enforces that the operator has access to the requested tenant. Access is granted if: 1. The
   * operator belongs to the tenant and has at least one of the service permissions. 2. The operator
   * has a global "cross-tenant" permission (e.g. for support/platform admins).
   */
  public void requireTenantAccess(AuthenticatedUser principal, String tenantId, String serviceKey) {
    // We use a generic naming convention for permissions: {serviceKey}.read/write
    // Since this is a generic facade, we check for a general read permission for the service.
    // Specific domain services will perform more fine-grained checks.
    String permission = serviceKey + ".read";
    String crossTenantPermission = "platform.tenants.read";

    requireTenantAccess(principal, tenantId, permission, crossTenantPermission);
  }
}
