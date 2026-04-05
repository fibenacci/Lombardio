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
package io.lombardio.identity.portal.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "customer_portal_credentials")
public class CustomerPortalCredentialEntity {

  @Id
  @Column(name = "customer_id", nullable = false)
  private String customerId;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Column(name = "activated_at", nullable = false)
  private Instant activatedAt;

  public String getCustomerId() {
    return customerId;
  }

  public void setCustomerId(@NotNull String customerId) {
    this.customerId = Objects.requireNonNull(customerId, "customerId");
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(@NotNull String passwordHash) {
    this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash");
  }

  public Instant getActivatedAt() {
    return activatedAt;
  }

  public void setActivatedAt(@NotNull Instant activatedAt) {
    this.activatedAt = Objects.requireNonNull(activatedAt, "activatedAt");
  }
}
