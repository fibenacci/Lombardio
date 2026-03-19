package io.lombardio.platform.tenant.domain;

import java.time.Instant;

public record Tenant(
        String id,
        String key,
        String displayName,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
}
