package io.lombardio.platform.tenant.api;

import java.time.Instant;

public record TenantFeatureResponse(
        String tenantId,
        String featureKey,
        boolean enabled,
        Instant updatedAt
) {
}
