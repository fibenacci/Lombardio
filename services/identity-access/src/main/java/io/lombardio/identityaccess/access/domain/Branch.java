package io.lombardio.identityaccess.access.domain;

import java.time.Instant;

public record Branch(
        String id,
        String tenantId,
        String key,
        String displayName,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
}
