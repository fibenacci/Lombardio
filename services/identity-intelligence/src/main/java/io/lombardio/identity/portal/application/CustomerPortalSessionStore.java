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
package io.lombardio.identity.portal.application;

import java.time.Instant;
import java.util.Optional;

public interface CustomerPortalSessionStore {
  void deleteByCustomerId(String customerId);

  CustomerPortalSession save(CustomerPortalSession session);

  Optional<CustomerPortalSession> findByTokenHash(String tokenHash);

  Optional<CustomerPortalSession> findByToken(String token);

  void deleteByToken(String token);

  void deleteExpiredBefore(Instant instant);
}
