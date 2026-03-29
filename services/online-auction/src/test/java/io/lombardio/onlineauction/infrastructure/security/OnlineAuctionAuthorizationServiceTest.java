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
package io.lombardio.onlineauction.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.lombardio.platform.security.AuthenticatedUser;
import io.lombardio.platform.security.ForbiddenException;
import java.util.List;
import org.junit.jupiter.api.Test;

class OnlineAuctionAuthorizationServiceTest {

  private final OnlineAuctionAuthorizationService service = new OnlineAuctionAuthorizationService();

  @Test
  void acceptsLegacyAuctionReadPermissionForTenantAccess() {
    AuthenticatedUser user =
        new AuthenticatedUser(
            "user-1",
            "user-1",
            "tenant-default",
            false,
            "admin@lombardio.local",
            "Tenant Admin",
            List.of("auctions.read"));

    assertDoesNotThrow(() -> service.requireRead(user, "tenant-default"));
  }

  @Test
  void acceptsLegacyAuctionWritePermissionForTenantAccess() {
    AuthenticatedUser user =
        new AuthenticatedUser(
            "user-1",
            "user-1",
            "tenant-default",
            false,
            "admin@lombardio.local",
            "Tenant Admin",
            List.of("auctions.write"));

    assertDoesNotThrow(() -> service.requireWrite(user, "tenant-default"));
  }

  @Test
  void stillRejectsMissingReadPermission() {
    AuthenticatedUser user =
        new AuthenticatedUser(
            "user-1",
            "user-1",
            "tenant-default",
            false,
            "admin@lombardio.local",
            "Tenant Admin",
            List.of("customers.read"));

    assertThrows(ForbiddenException.class, () -> service.requireRead(user, "tenant-default"));
  }
}
