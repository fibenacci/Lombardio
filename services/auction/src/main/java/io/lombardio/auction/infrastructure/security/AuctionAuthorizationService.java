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
package io.lombardio.auction.infrastructure.security;

import io.lombardio.platform.security.AuthenticatedUser;
import io.lombardio.platform.security.BaseAuthorizationService;
import org.springframework.stereotype.Service;

@Service
public class AuctionAuthorizationService extends BaseAuthorizationService {

  public void requireRead(AuthenticatedUser user, String tenantId) {
    requireTenantAccess(user, tenantId, "auctions.read", "platform.tenants.read");
  }

  public void requireWrite(AuthenticatedUser user, String tenantId) {
    requireTenantAccess(user, tenantId, "auctions.write", "platform.tenants.write");
  }
}
