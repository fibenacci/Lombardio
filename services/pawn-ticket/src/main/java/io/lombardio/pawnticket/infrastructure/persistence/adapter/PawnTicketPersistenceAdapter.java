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

import io.lombardio.pawnticket.domain.model.PawnTicket;
import io.lombardio.pawnticket.domain.port.PawnTicketRepository;
import io.lombardio.pawnticket.infrastructure.persistence.mapper.PersistenceMapper;
import io.lombardio.pawnticket.infrastructure.persistence.repository.SpringDataPawnTicketRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PawnTicketPersistenceAdapter implements PawnTicketRepository {

  private final SpringDataPawnTicketRepository repository;
  private final PersistenceMapper mapper;

  @Override
  public PawnTicket save(PawnTicket pawnTicket) {
    return mapper.toDomain(repository.save(mapper.toEntity(pawnTicket)));
  }

  @Override
  public Optional<PawnTicket> findByTicketNumber(String ticketNumber) {
    return repository.findByTicketNumber(ticketNumber).map(mapper::toDomain);
  }

  @Override
  public List<PawnTicket> findByTenantId(String tenantId) {
    return repository.findByTenantIdOrderByCreatedAtDesc(tenantId).stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Override
  public List<PawnTicket> findByTenantIdAndCustomerId(String tenantId, String customerId) {
    return repository.findByTenantIdAndCustomerIdOrderByCreatedAtDesc(tenantId, customerId).stream()
        .map(mapper::toDomain)
        .toList();
  }
}
