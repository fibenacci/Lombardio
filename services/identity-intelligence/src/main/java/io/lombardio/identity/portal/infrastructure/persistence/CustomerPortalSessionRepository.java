package io.lombardio.identity.portal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerPortalSessionRepository extends JpaRepository<CustomerPortalSessionEntity, String> {

    void deleteByCustomerId(String customerId);
}
