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
import io.lombardio.platform.security.ForbiddenException;
import io.lombardio.platform.security.UnauthorizedException;
import org.springframework.stereotype.Service;

@Service
public class TenantAdministrationAuthorizationService {

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

  private void requireTenantAccess(
      AuthenticatedUser user, String tenantId, String tenantPermission, String platformPermission) {
    if (user == null) {
      throw new UnauthorizedException("Authentication required");
    }
    if (user.hasPermission(platformPermission)) {
      return;
    }
    if (!user.hasPermission(tenantPermission)) {
      throw new ForbiddenException("Missing permission: " + tenantPermission);
    }
    if (tenantId != null && !tenantId.equals(user.tenantId())) {
      throw new ForbiddenException("Tenant access is limited to the effective tenant");
    }
  }
}
