package io.lombardio.platform.security;

import java.util.List;

public record AuthenticatedPlatformUser(
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
