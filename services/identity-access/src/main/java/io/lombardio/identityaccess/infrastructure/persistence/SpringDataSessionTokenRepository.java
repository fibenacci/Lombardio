package io.lombardio.identityaccess.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataSessionTokenRepository extends JpaRepository<SessionTokenEntity, String> {

    void deleteByUserId(String userId);
}
