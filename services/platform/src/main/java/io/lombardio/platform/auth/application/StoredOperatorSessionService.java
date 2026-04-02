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
package io.lombardio.platform.auth.application;

import io.lombardio.platform.auth.infrastructure.persistence.OperatorSessionEntity;
import io.lombardio.platform.auth.infrastructure.persistence.SpringDataOperatorSessionRepository;
import io.lombardio.platform.config.OperatorSessionProperties;
import io.lombardio.platform.security.AuthenticatedUser;
import io.lombardio.platform.security.UnauthorizedException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StoredOperatorSessionService {

  private final SpringDataOperatorSessionRepository repository;
  private final OperatorSessionCrypto crypto;
  private final OperatorAuthService operatorAuthService;
  private final OperatorSessionProperties properties;
  private final SecureRandom secureRandom = new SecureRandom();

  public StoredOperatorSessionService(
      SpringDataOperatorSessionRepository repository,
      OperatorSessionCrypto crypto,
      OperatorAuthService operatorAuthService,
      OperatorSessionProperties properties) {
    this.repository = repository;
    this.crypto = crypto;
    this.operatorAuthService = operatorAuthService;
    this.properties = properties;
  }

  @Transactional
  public StoredOperatorSession createSession(OperatorSession session) {
    Instant now = Instant.now();
    OperatorSessionEntity entity = new OperatorSessionEntity();
    entity.setId(generateSessionId());
    entity.setAccessTokenCiphertext(crypto.encrypt(session.accessToken()));
    entity.setRefreshTokenCiphertext(crypto.encrypt(session.refreshToken()));
    entity.setCreatedAt(now);
    entity.setUpdatedAt(now);
    entity.setExpiresAt(now.plusSeconds(properties.cookieMaxAgeSeconds()));
    repository.save(entity);
    cleanupExpiredSessions(now);
    return new StoredOperatorSession(entity.getId(), session.user());
  }

  @Transactional
  public Optional<StoredOperatorSession> refreshSession(String sessionId) {
    return repository
        .findById(sessionId)
        .filter(entity -> !isExpired(entity))
        .flatMap(
            entity -> {
              try {
                String refreshToken = crypto.decrypt(entity.getRefreshTokenCiphertext());
                OperatorSession refreshed = operatorAuthService.refresh(refreshToken);
                updateTokens(entity, refreshed);
                return Optional.of(new StoredOperatorSession(entity.getId(), refreshed.user()));
              } catch (RuntimeException exception) {
                repository.delete(entity);
                return Optional.empty();
              }
            });
  }

  @Transactional
  public Optional<StoredOperatorAuthentication> authenticate(String sessionId) {
    return repository
        .findById(sessionId)
        .flatMap(
            entity -> {
              if (isExpired(entity)) {
                repository.delete(entity);
                return Optional.empty();
              }

              try {
                String accessToken = crypto.decrypt(entity.getAccessTokenCiphertext());
                AuthenticatedUser user = operatorAuthService.authenticatedUserFromAccessToken(accessToken);
                return Optional.of(new StoredOperatorAuthentication(accessToken, user));
              } catch (UnauthorizedException exception) {
                return refreshForAuthentication(entity);
              } catch (RuntimeException exception) {
                repository.delete(entity);
                return Optional.empty();
              }
            });
  }

  @Transactional
  public void logout(String sessionId) {
    if (sessionId == null || sessionId.isBlank()) {
      return;
    }
    repository
        .findById(sessionId)
        .ifPresent(
            entity -> {
              try {
                operatorAuthService.logout(crypto.decrypt(entity.getRefreshTokenCiphertext()));
              } catch (RuntimeException ignored) {
                // Removing the local session is more important than propagating logout noise.
              }
              repository.delete(entity);
            });
  }

  private Optional<StoredOperatorAuthentication> refreshForAuthentication(OperatorSessionEntity entity) {
    try {
      String refreshToken = crypto.decrypt(entity.getRefreshTokenCiphertext());
      OperatorSession refreshed = operatorAuthService.refresh(refreshToken);
      updateTokens(entity, refreshed);
      AuthenticatedUser user =
          operatorAuthService.authenticatedUserFromAccessToken(refreshed.accessToken());
      return Optional.of(new StoredOperatorAuthentication(refreshed.accessToken(), user));
    } catch (RuntimeException exception) {
      repository.delete(entity);
      return Optional.empty();
    }
  }

  private void updateTokens(OperatorSessionEntity entity, OperatorSession session) {
    Instant now = Instant.now();
    entity.setAccessTokenCiphertext(crypto.encrypt(session.accessToken()));
    entity.setRefreshTokenCiphertext(crypto.encrypt(session.refreshToken()));
    entity.setUpdatedAt(now);
    entity.setExpiresAt(now.plusSeconds(properties.cookieMaxAgeSeconds()));
    repository.save(entity);
  }

  private boolean isExpired(OperatorSessionEntity entity) {
    return entity.getExpiresAt().isBefore(Instant.now());
  }

  private void cleanupExpiredSessions(Instant now) {
    repository.deleteByExpiresAtBefore(now.minusSeconds(60));
  }

  private String generateSessionId() {
    byte[] bytes = new byte[32];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}
