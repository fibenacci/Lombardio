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
package io.lombardio.platform.auth.application;

import java.util.List;

/**
 * Domain-specific representation of an authenticated operator. This is a pure domain object without
 * any dependencies on security frameworks.
 */
public record Operator(
    String id,
    String actorUserId,
    String tenantId,
    boolean impersonating,
    String email,
    String displayName,
    List<String> roles,
    List<String> permissions) {

  public Operator {
    roles = List.copyOf(roles != null ? roles : List.of());
    permissions = List.copyOf(permissions != null ? permissions : List.of());
  }

  public boolean hasPermission(String permission) {
    return permissions.contains(permission);
  }
}
