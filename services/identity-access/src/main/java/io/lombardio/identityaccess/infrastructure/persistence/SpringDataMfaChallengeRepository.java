package io.lombardio.identityaccess.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataMfaChallengeRepository extends JpaRepository<MfaChallengeEntity, String> {

    void deleteByUserId(String userId);
}
