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
import io.lombardio.identity.portal.application.CustomerPortalSession;
import io.lombardio.identity.portal.application.CustomerPortalSessionStore;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class CustomerPortalSessionStoreAdapter implements CustomerPortalSessionStore {

  private final CustomerPortalSessionRepository repository;

  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring managed repository proxy")
  public CustomerPortalSessionStoreAdapter(CustomerPortalSessionRepository repository) {
    this.repository = repository;
  }

  @Override
  public void deleteByCustomerId(String customerId) {
    repository.deleteByCustomerId(customerId);
  }

  @Override
  public CustomerPortalSession save(CustomerPortalSession session) {
    CustomerPortalSessionEntity entity = new CustomerPortalSessionEntity();
    entity.setToken(session.token());
    entity.setTokenHash(session.tokenHash());
    entity.setCustomerId(session.customerId());
    entity.setTenantId(session.tenantId());
    entity.setIssuedAt(session.issuedAt());
    entity.setExpiresAt(session.expiresAt());
    return toModel(repository.save(entity));
  }

  @Override
  public Optional<CustomerPortalSession> findByTokenHash(String tokenHash) {
    return repository.findByTokenHash(tokenHash).map(this::toModel);
  }

  @Override
  public Optional<CustomerPortalSession> findByToken(String token) {
    return repository.findById(token).map(this::toModel);
  }

  @Override
  public void deleteByToken(String token) {
    repository.deleteById(token);
  }

  @Override
  public void deleteExpiredBefore(Instant instant) {
    repository.deleteByExpiresAtBefore(instant);
  }

  private CustomerPortalSession toModel(CustomerPortalSessionEntity entity) {
    return new CustomerPortalSession(
        entity.getToken(),
        entity.getTokenHash(),
        entity.getCustomerId(),
        entity.getTenantId(),
        entity.getIssuedAt(),
        entity.getExpiresAt());
  }
}
