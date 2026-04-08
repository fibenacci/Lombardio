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

import io.lombardio.loanorigination.domain.model.ValuationGuideline;
import io.lombardio.loanorigination.domain.port.ValuationGuidelineRepository;
import io.lombardio.loanorigination.infrastructure.persistence.mapper.PersistenceMapper;
import io.lombardio.loanorigination.infrastructure.persistence.repository.SpringDataValuationGuidelineRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public final class ValuationGuidelinePersistenceAdapter implements ValuationGuidelineRepository {

  private final SpringDataValuationGuidelineRepository repository;
  private final PersistenceMapper mapper;

  public ValuationGuidelinePersistenceAdapter(
      SpringDataValuationGuidelineRepository repository, PersistenceMapper mapper) {
    this.repository = Objects.requireNonNull(repository);
    this.mapper = Objects.requireNonNull(mapper);
  }

  @Override
  public ValuationGuideline save(ValuationGuideline guideline) {
    return mapper.toDomain(repository.save(mapper.toEntity(guideline)));
  }

  @Override
  public Optional<ValuationGuideline> findById(String id) {
    return repository.findById(id).map(entity -> mapper.toDomain(entity));
  }

  @Override
  public List<ValuationGuideline> findByTenantId(String tenantId) {
    return repository.findByTenantIdOrderByCategoryAscLabelAsc(tenantId).stream()
        .map(entity -> mapper.toDomain(entity))
        .toList();
  }
}
