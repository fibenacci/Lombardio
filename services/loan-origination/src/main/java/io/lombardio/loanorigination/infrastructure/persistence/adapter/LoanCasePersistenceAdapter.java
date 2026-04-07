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
package io.lombardio.loanorigination.infrastructure.persistence.adapter;

import io.lombardio.loanorigination.domain.model.LoanCase;
import io.lombardio.loanorigination.domain.port.LoanCaseRepository;
import io.lombardio.loanorigination.infrastructure.persistence.entity.LoanCaseEntity;
import io.lombardio.loanorigination.infrastructure.persistence.mapper.PersistenceMapper;
import io.lombardio.loanorigination.infrastructure.persistence.repository.SpringDataLoanCaseRepository;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public final class LoanCasePersistenceAdapter implements LoanCaseRepository {

  private final SpringDataLoanCaseRepository repository;
  private final PersistenceMapper mapper;

  public LoanCasePersistenceAdapter(
      SpringDataLoanCaseRepository repository, PersistenceMapper mapper) {
    this.repository = Objects.requireNonNull(repository);
    this.mapper = Objects.requireNonNull(mapper);
  }

  @Override
  public LoanCase save(LoanCase loanCase) {
    LoanCaseEntity entity = mapper.toEntity(loanCase);
    mapper.linkEntities(entity, loanCase);
    return mapper.toDomain(repository.save(entity));
  }

  @Override
  public List<LoanCase> findByTenantId(String tenantId) {
    return repository.findByTenantId(tenantId).stream()
        .map(entity -> mapper.toDomain(entity))
        .toList();
  }

  @Override
  public List<LoanCase> findByTenantIdAndCustomerId(String tenantId, String customerId) {
    return repository.findByTenantIdAndCustomerId(tenantId, customerId).stream()
        .map(entity -> mapper.toDomain(entity))
        .toList();
  }
}
