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

import io.lombardio.loanorigination.domain.model.PledgeRecord;
import io.lombardio.loanorigination.domain.port.PledgeRecordRepository;
import io.lombardio.loanorigination.infrastructure.persistence.mapper.PersistenceMapper;
import io.lombardio.loanorigination.infrastructure.persistence.repository.SpringDataPledgeRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PledgeRecordPersistenceAdapter implements PledgeRecordRepository {

  private final SpringDataPledgeRecordRepository repository;
  private final PersistenceMapper mapper;

  @Override
  public PledgeRecord save(PledgeRecord pledgeRecord) {
    return mapper.toDomain(repository.save(mapper.toEntity(pledgeRecord)));
  }
}
