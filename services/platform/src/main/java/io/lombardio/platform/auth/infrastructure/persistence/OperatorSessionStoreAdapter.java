package io.lombardio.platform.auth.infrastructure.persistence;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.lombardio.platform.auth.application.OperatorSessionStore;
import io.lombardio.platform.auth.application.PersistedOperatorSession;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class OperatorSessionStoreAdapter implements OperatorSessionStore {

  private final SpringDataOperatorSessionRepository repository;

  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring managed repository proxy")
  public OperatorSessionStoreAdapter(SpringDataOperatorSessionRepository repository) {
    this.repository = repository;
  }

  @Override
  public PersistedOperatorSession save(PersistedOperatorSession session) {
    return toModel(repository.save(toEntity(session)));
  }

  @Override
  public Optional<PersistedOperatorSession> findById(String sessionId) {
    return repository.findById(sessionId).map(this::toModel);
  }

  @Override
  public void deleteById(String sessionId) {
    repository.deleteById(sessionId);
  }

  @Override
  public void deleteExpiredBefore(Instant cutoff) {
    repository.deleteByExpiresAtBefore(cutoff);
  }

  private PersistedOperatorSession toModel(OperatorSessionEntity entity) {
    return new PersistedOperatorSession(
        entity.getId(),
        entity.getAccessTokenCiphertext(),
        entity.getRefreshTokenCiphertext(),
        entity.getExpiresAt(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }

  private OperatorSessionEntity toEntity(PersistedOperatorSession session) {
    OperatorSessionEntity entity = new OperatorSessionEntity();
    entity.setId(session.id());
    entity.setAccessTokenCiphertext(session.accessTokenCiphertext());
    entity.setRefreshTokenCiphertext(session.refreshTokenCiphertext());
    entity.setExpiresAt(session.expiresAt());
    entity.setCreatedAt(session.createdAt());
    entity.setUpdatedAt(session.updatedAt());
    return entity;
  }
}
