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

import java.time.Instant;
import java.util.Optional;

public interface OperatorSessionStore {

  PersistedOperatorSession save(PersistedOperatorSession session);

  Optional<PersistedOperatorSession> findById(String sessionId);

  void deleteById(String sessionId);

  void deleteExpiredBefore(Instant cutoff);
}
