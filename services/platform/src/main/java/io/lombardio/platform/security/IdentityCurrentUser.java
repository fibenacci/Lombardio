package io.lombardio.platform.security;

import java.util.List;

public record IdentityCurrentUser(
        String id,
        String actorUserId,
        String tenantId,
        boolean impersonating,
        List<String> permissions
) {
}
