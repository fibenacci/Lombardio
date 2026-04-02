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
import java.time.Instant;

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

  public void setAccessTokenCiphertext(String accessTokenCiphertext) {
    this.accessTokenCiphertext = accessTokenCiphertext;
  }

  public String getRefreshTokenCiphertext() {
    return refreshTokenCiphertext;
  }

  public void setRefreshTokenCiphertext(String refreshTokenCiphertext) {
    this.refreshTokenCiphertext = refreshTokenCiphertext;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(Instant expiresAt) {
    this.expiresAt = expiresAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}
