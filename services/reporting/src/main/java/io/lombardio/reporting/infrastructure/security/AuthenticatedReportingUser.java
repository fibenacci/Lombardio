package io.lombardio.reporting.infrastructure.security;

import java.util.List;

public record AuthenticatedReportingUser(
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
