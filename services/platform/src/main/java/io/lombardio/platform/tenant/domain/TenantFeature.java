package io.lombardio.platform.tenant.domain;

import java.time.Instant;

public record TenantFeature(
        String tenantId,
        String featureKey,
        boolean enabled,
        Instant updatedAt
) {
}
