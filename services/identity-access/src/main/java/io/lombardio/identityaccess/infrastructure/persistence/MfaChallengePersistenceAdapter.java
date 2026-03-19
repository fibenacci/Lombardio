package io.lombardio.identityaccess.infrastructure.persistence;

import io.lombardio.identityaccess.auth.domain.MfaChallenge;
import io.lombardio.identityaccess.auth.domain.MfaChallengeRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
public class MfaChallengePersistenceAdapter implements MfaChallengeRepository {

    private final SpringDataMfaChallengeRepository repository;

    public MfaChallengePersistenceAdapter(SpringDataMfaChallengeRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<MfaChallenge> findById(String id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public MfaChallenge save(MfaChallenge challenge) {
        return toDomain(repository.save(toEntity(challenge)));
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }

    @Override
    public void deleteByUserId(String userId) {
        repository.deleteByUserId(userId);
    }

    private MfaChallengeEntity toEntity(MfaChallenge challenge) {
        MfaChallengeEntity entity = new MfaChallengeEntity();
        entity.setId(challenge.id());
        entity.setUserId(challenge.userId());
        entity.setTenantId(challenge.tenantId());
        entity.setFactorType(challenge.factorType());
        entity.setCreatedAt(challenge.createdAt());
        entity.setExpiresAt(challenge.expiresAt());
        return entity;
    }

    private MfaChallenge toDomain(MfaChallengeEntity entity) {
        return new MfaChallenge(
                entity.getId(),
                entity.getUserId(),
                entity.getTenantId(),
                entity.getFactorType(),
                entity.getCreatedAt(),
                entity.getExpiresAt()
        );
    }
}
