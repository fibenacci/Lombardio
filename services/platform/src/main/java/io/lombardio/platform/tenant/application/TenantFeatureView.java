package io.lombardio.platform.tenant.application;

import java.time.Instant;

public record TenantFeatureView(
    String tenantId, String featureKey, boolean enabled, Instant updatedAt) {}
