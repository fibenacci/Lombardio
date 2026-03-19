package io.lombardio.identityaccess.access.domain;

import java.time.Instant;
import java.util.List;

public record User(
        String id,
        String tenantId,
        List<String> branchIds,
        String username,
        String email,
        String passwordHash,
        String displayName,
        String status,
        List<String> roleIds,
        Instant createdAt,
        Instant updatedAt
) {
    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }
}
