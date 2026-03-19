package io.lombardio.identityaccess.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataTotpCredentialRepository extends JpaRepository<TotpCredentialEntity, String> {
}
