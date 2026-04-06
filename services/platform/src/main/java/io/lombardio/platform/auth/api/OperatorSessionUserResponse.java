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
package io.lombardio.platform.auth.api;

import io.lombardio.platform.auth.application.OperatorSessionUserView;
import java.util.List;

public record OperatorSessionUserResponse(
    String id,
    String actorUserId,
    String tenantId,
    String email,
    String displayName,
    boolean impersonating,
    List<String> roles,
    List<String> permissions) {

  public OperatorSessionUserResponse {
    roles = List.copyOf(roles != null ? roles : List.of());
    permissions = List.copyOf(permissions != null ? permissions : List.of());
  }

  public static OperatorSessionUserResponse fromView(OperatorSessionUserView user) {
    return new OperatorSessionUserResponse(
        user.id(),
        user.actorUserId(),
        user.tenantId(),
        user.email(),
        user.displayName(),
        user.impersonating(),
        user.permissions(),
        user.permissions());
  }
}
