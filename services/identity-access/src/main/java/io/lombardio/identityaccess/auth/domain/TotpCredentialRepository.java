package io.lombardio.identityaccess.auth.domain;

import java.util.Optional;

public interface TotpCredentialRepository {

    Optional<TotpCredential> findByUserId(String userId);

    TotpCredential save(TotpCredential credential);

    void deleteByUserId(String userId);
}
