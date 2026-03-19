package io.lombardio.identityaccess.auth.domain;

import java.util.Optional;

public interface MfaChallengeRepository {

    Optional<MfaChallenge> findById(String id);

    MfaChallenge save(MfaChallenge challenge);

    void deleteById(String id);

    void deleteByUserId(String userId);
}
