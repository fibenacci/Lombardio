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
package io.lombardio.platform.tenant.api;

import io.lombardio.platform.security.AuthenticatedUser;
import io.lombardio.platform.security.BaseAuthorizationService;
import org.springframework.stereotype.Service;

@Service
public class TenantAdministrationAuthorizationService extends BaseAuthorizationService {

  public void requireTenantUserRead(AuthenticatedUser user, String tenantId) {
    requireTenantAccess(user, tenantId, "users.read", "platform.tenants.read");
  }

  public void requireTenantUserWrite(AuthenticatedUser user, String tenantId) {
    requireTenantAccess(user, tenantId, "users.write", "platform.tenants.write");
  }

  public void requireTenantRoleRead(AuthenticatedUser user, String tenantId) {
    requireTenantAccess(user, tenantId, "roles.read", "platform.tenants.read");
  }

  public void requireTenantBranchRead(AuthenticatedUser user, String tenantId) {
    requireTenantAccess(user, tenantId, "branches.read", "platform.tenants.read");
  }

  public void requireTenantBranchWrite(AuthenticatedUser user, String tenantId) {
    requireTenantAccess(user, tenantId, "branches.write", "platform.tenants.write");
  }

  public void requireTenantFeatureRead(AuthenticatedUser user, String tenantId) {
    requireTenantMatchOrPermission(user, tenantId, "platform.tenants.read");
  }
}
