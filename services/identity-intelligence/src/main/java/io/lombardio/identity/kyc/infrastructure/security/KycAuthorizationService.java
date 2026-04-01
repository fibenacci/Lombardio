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
package io.lombardio.identity.kyc.infrastructure.security;

import io.lombardio.platform.security.AuthenticatedUser;
import io.lombardio.platform.security.BaseAuthorizationService;
import io.lombardio.platform.security.ForbiddenException;
import io.lombardio.platform.security.UnauthorizedException;
import org.springframework.stereotype.Service;

@Service
public class KycAuthorizationService extends BaseAuthorizationService {

  public void requireRead(AuthenticatedUser user, String tenantId) {
    requireTenantAccess(user, tenantId, "kyc.read", "platform.tenants.read");
  }

  public void requireWrite(AuthenticatedUser user, String tenantId) {
    requireTenantAccess(user, tenantId, "kyc.write", "platform.tenants.write");
  }

  public void requireDocumentRead(AuthenticatedUser user, String tenantId) {
    if (user == null) {
      throw new UnauthorizedException("Authentication required");
    }
    if (user.hasPermission("platform.tenants.read")) {
      return;
    }
    if (!user.hasPermission("kyc.documents.read") && !user.hasPermission("kyc.read")) {
      throw new ForbiddenException("Missing permission: kyc.documents.read");
    }
    if (tenantId != null && !tenantId.equals(user.tenantId())) {
      throw new ForbiddenException("Tenant access is limited to the effective tenant");
    }
  }
}
