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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.lombardio.platform.security.AuthenticatedUser;
import io.lombardio.platform.security.ForbiddenException;
import java.util.List;
import org.junit.jupiter.api.Test;

class KycAuthorizationServiceTest {

  private final KycAuthorizationService service = new KycAuthorizationService();

  @Test
  void allowsDocumentReadWithGenericKycReadPermission() {
    AuthenticatedUser user =
        new AuthenticatedUser(
            "tenant-admin",
            "tenant-admin",
            "tenant-default",
            false,
            "admin@lombardio.local",
            "Tenant Admin",
            List.of("kyc.read"));

    assertDoesNotThrow(() -> service.requireDocumentRead(user, "tenant-default"));
  }

  @Test
  void rejectsDocumentReadWithoutKycReadPermission() {
    AuthenticatedUser user =
        new AuthenticatedUser(
            "customer-agent",
            "customer-agent",
            "tenant-default",
            false,
            "agent@lombardio.local",
            "Customer Agent",
            List.of("customers.read"));

    assertThrows(
        ForbiddenException.class, () -> service.requireDocumentRead(user, "tenant-default"));
  }
}
