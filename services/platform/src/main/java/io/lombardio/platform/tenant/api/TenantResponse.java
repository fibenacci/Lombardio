package io.lombardio.platform.tenant.api;

import java.time.Instant;

public record TenantResponse(
        String id,
        String key,
        String displayName,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
}
