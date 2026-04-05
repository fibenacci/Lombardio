/*
 * Lombardio Source-Available No-Distribution License 1.0
 *
 * Copyright (c) 2026 Benjamin Letzel. All rights reserved.
 *
 * This project is source-available for educational and review purposes only.
 * Redistribution, sublicensing, or commercial use is strictly prohibited.
 *
 * For partnership or cooperation inquiries, please contact the author.
 */
package io.lombardio.platform.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class TenantFeatureId implements Serializable {
  private static final long serialVersionUID = 1L;

  @Column(name = "tenant_id", nullable = false)
  private String tenantId;

  @Column(name = "feature_key", nullable = false)
  private String featureKey;

  public TenantFeatureId() {}

  public TenantFeatureId(String tenantId, String featureKey) {
    this.tenantId = tenantId;
    this.featureKey = featureKey;
  }

  public TenantFeatureId(TenantFeatureId other) {
    this(other.tenantId, other.featureKey);
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
