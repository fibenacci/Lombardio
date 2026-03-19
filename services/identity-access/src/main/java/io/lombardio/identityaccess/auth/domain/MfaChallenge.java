package io.lombardio.identityaccess.auth.domain;

import java.time.Instant;

public record MfaChallenge(
        String id,
        String userId,
        String tenantId,
        String factorType,
        Instant createdAt,
        Instant expiresAt
) {
    public boolean isExpired(Instant now) {
        return expiresAt.isBefore(now);
    }
}
