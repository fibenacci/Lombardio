package io.lombardio.kyc.domain;

public interface TenantFeatureDirectory {

    boolean isFeatureEnabled(String tenantId, String featureKey);
}
