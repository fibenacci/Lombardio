package io.lombardio.aml.domain.port;

public interface TenantFeatureDirectory {

    boolean isFeatureEnabled(String tenantId, String featureKey);
}
