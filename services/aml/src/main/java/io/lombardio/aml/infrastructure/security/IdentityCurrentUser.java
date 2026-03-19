package io.lombardio.aml.infrastructure.security;

import java.util.List;

public record IdentityCurrentUser(
        String id,
        String actorUserId,
        String tenantId,
        boolean impersonating,
        List<String> permissions
) {
}
