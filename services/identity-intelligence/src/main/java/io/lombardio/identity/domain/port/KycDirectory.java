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
package io.lombardio.identity.domain.port;

import java.util.Optional;

@FunctionalInterface
public interface KycDirectory {
  KycProjection getStatus(String tenantId, String customerId, Optional<String> accessToken);

  default KycProjection getStatus(String tenantId, String customerId) {
    return getStatus(tenantId, customerId, Optional.empty());
  }

  record KycProjection(String status, boolean approved, String documentType) {}
}
