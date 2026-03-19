package io.lombardio.identityaccess.auth.domain;

import java.time.Instant;

public record SessionToken(
        String token,
        String actorUserId,
        String userId,
        String tenantId,
        Instant issuedAt
) {
}
