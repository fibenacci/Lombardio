package io.lombardio.identityaccess.auth.api;

import java.util.List;

public record CurrentUserResponse(
        String id,
        String actorUserId,
        String tenantId,
        String username,
        String email,
        String displayName,
        String status,
        boolean impersonating,
        boolean mfaEnabled,
        List<String> mfaMethods,
        List<String> roles,
        List<String> permissions
) {
}
