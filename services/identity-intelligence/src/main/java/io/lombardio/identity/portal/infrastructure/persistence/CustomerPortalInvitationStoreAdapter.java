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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.lombardio.identity.portal.application.CustomerPortalInvitation;
import io.lombardio.identity.portal.application.CustomerPortalInvitationStore;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class CustomerPortalInvitationStoreAdapter implements CustomerPortalInvitationStore {

  private final CustomerPortalInvitationRepository repository;

  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring managed repository proxy")
  public CustomerPortalInvitationStoreAdapter(CustomerPortalInvitationRepository repository) {
    this.repository = repository;
  }

  @Override
  public void deleteUnusedByCustomerId(String customerId) {
    repository.deleteByCustomerIdAndUsedAtIsNull(customerId);
  }

  @Override
  public CustomerPortalInvitation save(CustomerPortalInvitation invitation) {
    CustomerPortalInvitationEntity entity = new CustomerPortalInvitationEntity();
    entity.setToken(invitation.token());
    entity.setTokenHash(invitation.tokenHash());
    entity.setCustomerId(invitation.customerId());
    entity.setTenantId(invitation.tenantId());
    entity.setEmail(invitation.email());
    entity.setIssuedAt(invitation.issuedAt());
    entity.setExpiresAt(invitation.expiresAt());
    entity.setUsedAt(invitation.usedAt());
    return toModel(repository.save(entity));
  }

  @Override
  public Optional<CustomerPortalInvitation> findByTokenHash(String tokenHash) {
    return repository.findByTokenHash(tokenHash).map(this::toModel);
  }

  @Override
  public Optional<CustomerPortalInvitation> findByToken(String token) {
    return repository.findById(token).map(this::toModel);
  }

  private CustomerPortalInvitation toModel(CustomerPortalInvitationEntity entity) {
    return new CustomerPortalInvitation(
        entity.getToken(),
        entity.getTokenHash(),
        entity.getCustomerId(),
        entity.getTenantId(),
        entity.getEmail(),
        entity.getIssuedAt(),
        entity.getExpiresAt(),
        entity.getUsedAt());
  }
}
