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

import io.lombardio.identity.portal.application.CustomerPortalCredential;
import io.lombardio.identity.portal.application.CustomerPortalCredentialStore;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class CustomerPortalCredentialStoreAdapter implements CustomerPortalCredentialStore {

  private final CustomerPortalCredentialRepository repository;

  public CustomerPortalCredentialStoreAdapter(CustomerPortalCredentialRepository repository) {
    this.repository = repository;
  }

  @Override
  public Optional<CustomerPortalCredential> findByCustomerId(String customerId) {
    return repository.findById(customerId).map(this::toModel);
  }

  @Override
  public CustomerPortalCredential save(CustomerPortalCredential credential) {
    CustomerPortalCredentialEntity entity = new CustomerPortalCredentialEntity();
    entity.setCustomerId(credential.customerId());
    entity.setPasswordHash(credential.passwordHash());
    entity.setActivatedAt(credential.activatedAt());
    return toModel(repository.save(entity));
  }

  private CustomerPortalCredential toModel(CustomerPortalCredentialEntity entity) {
    return new CustomerPortalCredential(
        entity.getCustomerId(), entity.getPasswordHash(), entity.getActivatedAt());
  }
}
