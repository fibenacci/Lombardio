package io.lombardio.identity.portal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerPortalInvitationRepository extends JpaRepository<CustomerPortalInvitationEntity, String> {

    void deleteByCustomerIdAndUsedAtIsNull(String customerId);
}
