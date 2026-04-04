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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "tenant_features")
public class TenantFeatureEntity {

  @EmbeddedId private TenantFeatureId id;

  @Column(name = "enabled", nullable = false)
  private boolean enabled;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "JPA requirement")
  public TenantFeatureId getId() {
    return id;
  }

  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "JPA requirement")
  public void setId(TenantFeatureId id) {
    this.id = id;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}
