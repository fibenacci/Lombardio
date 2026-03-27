package io.lombardio.identity.portal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerPortalCredentialRepository extends JpaRepository<CustomerPortalCredentialEntity, String> {
}
