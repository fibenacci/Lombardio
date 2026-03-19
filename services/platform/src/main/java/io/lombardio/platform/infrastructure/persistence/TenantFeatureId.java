package io.lombardio.platform.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class TenantFeatureId implements Serializable {

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "feature_key", nullable = false)
    private String featureKey;

    public TenantFeatureId() {
    }

    public TenantFeatureId(String tenantId, String featureKey) {
        this.tenantId = tenantId;
        this.featureKey = featureKey;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getFeatureKey() {
        return featureKey;
    }

    public void setFeatureKey(String featureKey) {
        this.featureKey = featureKey;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof TenantFeatureId that)) {
            return false;
        }
        return Objects.equals(tenantId, that.tenantId) && Objects.equals(featureKey, that.featureKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, featureKey);
    }
}
