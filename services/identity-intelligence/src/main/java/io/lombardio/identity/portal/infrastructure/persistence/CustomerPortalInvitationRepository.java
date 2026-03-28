/*
 * Lombardio Source-Available No-Distribution License 1.0
 *
 * Copyright (c) 2026 Benjamin Letzel. All rights reserved.
 *
 * This project is source-available for educational and review purposes only.
 * Redistribution, sublicensing, or commercial use is strictly prohibited.
 *
 * For partnership or cooperation inquiries, please contact the author.
 */
package io.lombardio.identity.portal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerPortalInvitationRepository
    extends JpaRepository<CustomerPortalInvitationEntity, String> {

  void deleteByCustomerIdAndUsedAtIsNull(String customerId);
}
