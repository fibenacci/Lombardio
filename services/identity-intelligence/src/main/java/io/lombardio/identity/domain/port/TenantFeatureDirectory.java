package io.lombardio.identity.domain.port;

public interface TenantFeatureDirectory {
    boolean isFeatureEnabled(String tenantId, String featureKey);
}
