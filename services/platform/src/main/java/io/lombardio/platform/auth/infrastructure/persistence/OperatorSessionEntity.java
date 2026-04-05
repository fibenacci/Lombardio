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
package io.lombardio.platform.auth.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "operator_sessions")
public class OperatorSessionEntity {

  @Id private String id;

  @Column(name = "access_token_ciphertext", nullable = false)
  private String accessTokenCiphertext;

  @Column(name = "refresh_token_ciphertext", nullable = false)
  private String refreshTokenCiphertext;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

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

  public String getAccessTokenCiphertext() {
    return accessTokenCiphertext;
  }

  public void setAccessTokenCiphertext(@NotNull String accessTokenCiphertext) {
    this.accessTokenCiphertext =
        Objects.requireNonNull(accessTokenCiphertext, "accessTokenCiphertext");
  }

  public String getRefreshTokenCiphertext() {
    return refreshTokenCiphertext;
  }

  public void setRefreshTokenCiphertext(@NotNull String refreshTokenCiphertext) {
    this.refreshTokenCiphertext =
        Objects.requireNonNull(refreshTokenCiphertext, "refreshTokenCiphertext");
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(@NotNull Instant expiresAt) {
    this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt");
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
