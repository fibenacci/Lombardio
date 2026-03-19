package io.lombardio.identityaccess.infrastructure.persistence;

import io.lombardio.identityaccess.auth.domain.TotpCredential;
import io.lombardio.identityaccess.auth.domain.TotpCredentialRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
public class TotpCredentialPersistenceAdapter implements TotpCredentialRepository {

    private final SpringDataTotpCredentialRepository repository;

    public TotpCredentialPersistenceAdapter(SpringDataTotpCredentialRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<TotpCredential> findByUserId(String userId) {
        return repository.findById(userId).map(this::toDomain);
    }

    @Override
    public TotpCredential save(TotpCredential credential) {
        return toDomain(repository.save(toEntity(credential)));
    }

    @Override
    public void deleteByUserId(String userId) {
        repository.deleteById(userId);
    }

    private TotpCredentialEntity toEntity(TotpCredential credential) {
        TotpCredentialEntity entity = new TotpCredentialEntity();
        entity.setUserId(credential.userId());
        entity.setSecretCiphertext(credential.secretCiphertext());
        entity.setEnabled(credential.enabled());
        entity.setCreatedAt(credential.createdAt());
        entity.setActivatedAt(credential.activatedAt());
        return entity;
    }

    private TotpCredential toDomain(TotpCredentialEntity entity) {
        return new TotpCredential(
                entity.getUserId(),
                entity.getSecretCiphertext(),
                entity.isEnabled(),
                entity.getCreatedAt(),
                entity.getActivatedAt()
        );
    }
}
