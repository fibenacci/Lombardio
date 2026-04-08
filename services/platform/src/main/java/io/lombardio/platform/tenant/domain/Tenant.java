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
package io.lombardio.platform.tenant.domain;

import java.time.Instant;

public record Tenant(
    String id,
    String key,
    String displayName,
    String status,
    Instant createdAt,
    Instant updatedAt) {

  public Tenant update(String key, String displayName, String status, Instant now) {
    return new Tenant(id, key, displayName, status, createdAt, now);
  }
}
