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

public record OperatorSessionUserView(
    String id,
    String actorUserId,
    String tenantId,
    String email,
    String displayName,
    boolean impersonating,
    List<String> roles,
    List<String> permissions) {

  public OperatorSessionUserView {
    roles = List.copyOf(roles != null ? roles : List.of());
    permissions = List.copyOf(permissions != null ? permissions : List.of());
  }

  public static OperatorSessionUserView fromOperator(Operator operator) {
    return new OperatorSessionUserView(
        operator.id(),
        operator.actorUserId(),
        operator.tenantId(),
        operator.email(),
        operator.displayName(),
        operator.impersonating(),
        operator.roles(),
        operator.permissions());
  }
}
