package io.lombardio.kyc.security;

import java.util.List;

public record AuthenticatedKycUser(
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
