package io.lombardio.platform.tenant.application;

import java.time.Instant;

public record TenantView(
    String id,
    String key,
    String displayName,
    String status,
    Instant createdAt,
    Instant updatedAt) {}
