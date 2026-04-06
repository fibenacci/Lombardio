package io.lombardio.platform.auth.application;

import java.time.Instant;
import java.util.Optional;

public interface OperatorSessionStore {

  PersistedOperatorSession save(PersistedOperatorSession session);

  Optional<PersistedOperatorSession> findById(String sessionId);

  void deleteById(String sessionId);

  void deleteExpiredBefore(Instant cutoff);
}
