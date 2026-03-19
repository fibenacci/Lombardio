package io.lombardio.identityaccess.access.api;

import java.time.Instant;
import java.util.List;

public record UserSummaryResponse(
        String id,
        String tenantId,
        List<String> branchIds,
        String username,
        String email,
        String displayName,
        String status,
        List<String> roleIds,
        Instant createdAt,
        Instant updatedAt
) {
}
