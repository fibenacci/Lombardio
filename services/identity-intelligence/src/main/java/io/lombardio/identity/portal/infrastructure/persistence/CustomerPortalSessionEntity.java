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
@Table(name = "customer_portal_sessions")
public class CustomerPortalSessionEntity {

  @Id
  @Column(name = "token", nullable = false)
  private String token;

  @Column(name = "token_hash")
  private String tokenHash;

  @Column(name = "customer_id", nullable = false)
  private String customerId;

  @Column(name = "tenant_id", nullable = false)
  private String tenantId;

  @Column(name = "issued_at", nullable = false)
  private Instant issuedAt;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  public String getToken() {
    return token;
  }

  public void setToken(@NotNull String token) {
    this.token = Objects.requireNonNull(token, "token");
  }

  public String getTokenHash() {
    return tokenHash;
  }

  public void setTokenHash(String tokenHash) {
    this.tokenHash = tokenHash;
  }

  public String getCustomerId() {
    return customerId;
  }

  public void setCustomerId(@NotNull String customerId) {
    this.customerId = Objects.requireNonNull(customerId, "customerId");
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(@NotNull String tenantId) {
    this.tenantId = Objects.requireNonNull(tenantId, "tenantId");
  }

  public Instant getIssuedAt() {
    return issuedAt;
  }

  public void setIssuedAt(@NotNull Instant issuedAt) {
    this.issuedAt = Objects.requireNonNull(issuedAt, "issuedAt");
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(@NotNull Instant expiresAt) {
    this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt");
  }
}
