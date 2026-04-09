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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.lombardio.platform.config.OperatorSessionProperties;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StoredOperatorSessionServiceTest {

  private OperatorSessionStore repository;
  private OperatorSessionCrypto crypto;
  private OperatorAuthService operatorAuthService;
  private OperatorSessionProperties properties;
  private StoredOperatorSessionService service;

  @BeforeEach
  void setUp() {
    repository = mock(OperatorSessionStore.class);
    operatorAuthService = mock(OperatorAuthService.class);
    properties =
        new OperatorSessionProperties(
            "lombardio-session", "/", false, "Lax", 3600, "9p4w3v-v3ry-s3cr3t-t3st-k3y-32ch");
    crypto = new OperatorSessionCrypto(properties);
    service = new StoredOperatorSessionService(repository, crypto, operatorAuthService, properties);
  }

  @Test
  void createSession_saves_and_returns_session_with_user_profile() {
    // Given
    String accessToken = "new-token";
    String refreshToken = "new-refresh";
    OperatorIdentityTokens tokens = new OperatorIdentityTokens(accessToken, refreshToken);
    OperatorSessionUserView user =
        new OperatorSessionUserView(
            "user-123",
            "actor-123",
            "tenant-123",
            "test@lombardio.io",
            "Test User",
            false,
            List.of(),
            List.of("PERM_1"));

    when(repository.save(any(PersistedOperatorSession.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(operatorAuthService.resolveProfile(accessToken)).thenReturn(user);

    // When
    OperatorSession result = service.createSession(tokens);

    // Then
    assertThat(result.sessionId()).isNotBlank();
    assertThat(result.user()).isEqualTo(user);
  }
}
