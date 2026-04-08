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
package io.lombardio.pawnticket.infrastructure.persistence.adapter;

import io.lombardio.pawnticket.domain.model.CashTransaction;
import io.lombardio.pawnticket.domain.port.CashTransactionRepository;
import io.lombardio.pawnticket.infrastructure.persistence.mapper.PersistenceMapper;
import io.lombardio.pawnticket.infrastructure.persistence.repository.SpringDataCashTransactionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CashTransactionPersistenceAdapter implements CashTransactionRepository {

  private final SpringDataCashTransactionRepository repository;
  private final PersistenceMapper mapper;

  @Override
  public CashTransaction save(CashTransaction cashTransaction) {
    return mapper.toDomain(repository.save(mapper.toEntity(cashTransaction)));
  }

  @Override
  public List<CashTransaction> findByTenantId(String tenantId) {
    return repository.findByTenantIdOrderByCreatedAtDesc(tenantId).stream()
        .map(mapper::toDomain)
        .toList();
  }
}
