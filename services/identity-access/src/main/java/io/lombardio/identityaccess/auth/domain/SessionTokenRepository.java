package io.lombardio.identityaccess.auth.domain;

import java.util.Optional;

public interface SessionTokenRepository {

    SessionToken save(SessionToken sessionToken);

    Optional<SessionToken> findByToken(String token);

    void deleteByToken(String token);

    void deleteByUserId(String userId);
}
