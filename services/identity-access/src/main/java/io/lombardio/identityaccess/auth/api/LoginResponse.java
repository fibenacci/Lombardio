package io.lombardio.identityaccess.auth.api;

import java.util.List;

public record LoginResponse(
        String status,
        String accessToken,
        String challengeId,
        String tokenType,
        String userId,
        String actorUserId,
        String tenantId,
        String displayName,
        boolean impersonating,
        List<String> permissions,
        List<String> mfaMethods
) {
}
