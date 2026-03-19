package io.lombardio.identityaccess.infrastructure.persistence;

import io.lombardio.identityaccess.auth.domain.SessionToken;
import io.lombardio.identityaccess.auth.domain.SessionTokenRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
public class SessionTokenPersistenceAdapter implements SessionTokenRepository {

    private final SpringDataSessionTokenRepository repository;

    public SessionTokenPersistenceAdapter(SpringDataSessionTokenRepository repository) {
        this.repository = repository;
    }

    @Override
    public SessionToken save(SessionToken sessionToken) {
        return toDomain(repository.save(toEntity(sessionToken)));
    }

    @Override
    public Optional<SessionToken> findByToken(String token) {
        return repository.findById(token).map(this::toDomain);
    }

    @Override
    public void deleteByToken(String token) {
        repository.deleteById(token);
    }

    @Override
    public void deleteByUserId(String userId) {
        repository.deleteByUserId(userId);
    }

    private SessionTokenEntity toEntity(SessionToken sessionToken) {
        SessionTokenEntity entity = new SessionTokenEntity();
        entity.setToken(sessionToken.token());
        entity.setActorUserId(sessionToken.actorUserId());
        entity.setUserId(sessionToken.userId());
        entity.setTenantId(sessionToken.tenantId());
        entity.setIssuedAt(sessionToken.issuedAt());
        return entity;
    }

    private SessionToken toDomain(SessionTokenEntity entity) {
        return new SessionToken(
                entity.getToken(),
                entity.getActorUserId(),
                entity.getUserId(),
                entity.getTenantId(),
                entity.getIssuedAt()
        );
    }
}
