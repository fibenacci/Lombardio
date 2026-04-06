package io.lombardio.platform.auth.application;

import io.lombardio.platform.security.AuthenticatedUser;
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

  public static OperatorSessionUserView fromAuthenticatedUser(AuthenticatedUser user) {
    return new OperatorSessionUserView(
        user.userId(),
        user.actorUserId(),
        user.tenantId(),
        user.email(),
        user.displayName(),
        user.impersonating(),
        user.permissions(),
        user.permissions());
  }
}
