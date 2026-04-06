package io.lombardio.platform.auth.application;

import java.time.Instant;

public record PersistedOperatorSession(
    String id,
    String accessTokenCiphertext,
    String refreshTokenCiphertext,
    Instant expiresAt,
    Instant createdAt,
    Instant updatedAt) {}
