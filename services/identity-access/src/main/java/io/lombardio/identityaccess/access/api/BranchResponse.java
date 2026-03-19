package io.lombardio.identityaccess.access.api;

import java.time.Instant;

public record BranchResponse(
        String id,
        String tenantId,
        String key,
        String displayName,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
}
