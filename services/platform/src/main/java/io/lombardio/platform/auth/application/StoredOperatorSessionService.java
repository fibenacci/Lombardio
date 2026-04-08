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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.lombardio.platform.config.OperatorSessionProperties;
import io.lombardio.platform.security.UnauthorizedException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StoredOperatorSessionService {

  private final OperatorSessionStore repository;
  private final OperatorSessionCrypto crypto;
  private final OperatorAuthService operatorAuthService;
  private final OperatorSessionProperties properties;
  private final SecureRandom secureRandom = new SecureRandom();

  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring managed repository proxy")
  public StoredOperatorSessionService(
      OperatorSessionStore repository,
      OperatorSessionCrypto crypto,
      OperatorAuthService operatorAuthService,
      OperatorSessionProperties properties) {
    this.repository = repository;
    this.crypto = crypto;
    this.operatorAuthService = operatorAuthService;
    this.properties = properties;
  }

  @Transactional
  public OperatorSession createSession(OperatorIdentityTokens tokens) {
    Instant now = Instant.now();
    PersistedOperatorSession entity =
        repository.save(
            new PersistedOperatorSession(
                generateSessionId(),
                crypto.encrypt(tokens.accessToken()),
                crypto.encrypt(tokens.refreshToken()),
                now.plusSeconds(properties.cookieMaxAgeSeconds()),
                now,
                now));
    cleanupExpiredSessions(now);
    OperatorSessionUserView user = operatorAuthService.resolveProfile(tokens.accessToken());
    return new OperatorSession(entity.id(), user);
  }

  @Transactional
  public Optional<OperatorSession> refreshSession(String sessionId) {
    return repository
        .findById(sessionId)
        .filter(entity -> !isExpired(entity))
        .flatMap(
            entity -> {
              try {
                String refreshToken = crypto.decrypt(entity.refreshTokenCiphertext());
                OperatorIdentityTokens refreshed = operatorAuthService.refresh(refreshToken);
                updateTokens(entity, refreshed);
                OperatorSessionUserView user =
                    operatorAuthService.resolveProfile(refreshed.accessToken());
                return Optional.of(new OperatorSession(entity.id(), user));
              } catch (RuntimeException exception) {
                repository.deleteById(entity.id());
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
                repository.deleteById(entity.id());
                return Optional.empty();
              }

              try {
                String accessToken = crypto.decrypt(entity.accessTokenCiphertext());
                Operator operator = operatorAuthService.resolveOperator(accessToken);
                return Optional.of(new StoredOperatorAuthentication(accessToken, operator));
              } catch (UnauthorizedException exception) {
                return refreshForAuthentication(entity);
              } catch (RuntimeException exception) {
                repository.deleteById(entity.id());
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
                operatorAuthService.logout(crypto.decrypt(entity.refreshTokenCiphertext()));
              } catch (RuntimeException ignored) {
                // Removing the local session is more important than propagating logout noise.
              }
              repository.deleteById(entity.id());
            });
  }

  private Optional<StoredOperatorAuthentication> refreshForAuthentication(
      PersistedOperatorSession entity) {
    try {
      String refreshToken = crypto.decrypt(entity.refreshTokenCiphertext());
      OperatorIdentityTokens refreshed = operatorAuthService.refresh(refreshToken);
      updateTokens(entity, refreshed);
      Operator operator = operatorAuthService.resolveOperator(refreshed.accessToken());
      return Optional.of(new StoredOperatorAuthentication(refreshed.accessToken(), operator));
    } catch (RuntimeException exception) {
      repository.deleteById(entity.id());
      return Optional.empty();
    }
  }

  private PersistedOperatorSession updateTokens(
      PersistedOperatorSession entity, OperatorIdentityTokens tokens) {
    Instant now = Instant.now();
    return repository.save(
        new PersistedOperatorSession(
            entity.id(),
            crypto.encrypt(tokens.accessToken()),
            crypto.encrypt(tokens.refreshToken()),
            now.plusSeconds(properties.cookieMaxAgeSeconds()),
            entity.createdAt(),
            now));
  }

  private boolean isExpired(PersistedOperatorSession entity) {
    return entity.expiresAt().isBefore(Instant.now());
  }

  private void cleanupExpiredSessions(Instant now) {
    repository.deleteExpiredBefore(now.minusSeconds(60));
  }

  private String generateSessionId() {
    byte[] bytes = new byte[32];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}
