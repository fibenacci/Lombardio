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
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "tenants")
public class TenantEntity {

  @Id private String id;

  @Column(name = "tenant_key", nullable = false)
  private String key;

  @Column(name = "display_name", nullable = false)
  private String displayName;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getKey() {
    return key;
  }

  public void setKey(@NotNull String key) {
    this.key = Objects.requireNonNull(key, "key");
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(@NotNull String displayName) {
    this.displayName = Objects.requireNonNull(displayName, "displayName");
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(@NotNull String status) {
    this.status = Objects.requireNonNull(status, "status");
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(@NotNull Instant createdAt) {
    this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(@NotNull Instant updatedAt) {
    this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
  }
}
