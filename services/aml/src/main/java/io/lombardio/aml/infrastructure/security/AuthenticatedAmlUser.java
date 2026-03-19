package io.lombardio.aml.infrastructure.security;

import java.util.List;

public record AuthenticatedAmlUser(
        String userId,
        String actorUserId,
        String tenantId,
        boolean impersonating,
        List<String> permissions
) {

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }
}
