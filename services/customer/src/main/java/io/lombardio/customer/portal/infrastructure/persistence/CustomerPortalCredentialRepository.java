package io.lombardio.customer.portal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerPortalCredentialRepository extends JpaRepository<CustomerPortalCredentialEntity, String> {
}
