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

public abstract class BaseAuthorizationService {

  protected void requireTenantAccess(
      AuthenticatedUser user, String tenantId, String permission, String crossTenantPermission) {
    if (user == null) {
      throw new UnauthorizedException("Authentication required");
    }
    if (!user.hasPermission(permission)) {
      throw new ForbiddenException("Missing permission: " + permission);
    }
    if (tenantId != null && !tenantId.equals(user.tenantId())) {
      if (crossTenantPermission == null || !user.hasPermission(crossTenantPermission)) {
        throw new ForbiddenException("Tenant access is limited to the effective tenant");
      }
    }
  }

  protected void requirePermission(AuthenticatedUser user, String permission) {
    if (user == null) {
      throw new UnauthorizedException("Authentication required");
    }
    if (!user.hasPermission(permission)) {
      throw new ForbiddenException("Missing permission: " + permission);
    }
  }
}
