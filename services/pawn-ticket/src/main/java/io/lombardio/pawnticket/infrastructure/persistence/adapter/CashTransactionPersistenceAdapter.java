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
import io.lombardio.pawnticket.infrastructure.persistence.entity.CashTransactionEntity;
import io.lombardio.pawnticket.infrastructure.persistence.repository.SpringDataCashTransactionRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class CashTransactionPersistenceAdapter implements CashTransactionRepository {

  private final SpringDataCashTransactionRepository repository;

  public CashTransactionPersistenceAdapter(SpringDataCashTransactionRepository repository) {
    this.repository = repository;
  }

  @Override
  public CashTransaction save(CashTransaction cashTransaction) {
    return toDomain(repository.save(toEntity(cashTransaction)));
  }

  @Override
  public List<CashTransaction> findByTenantId(String tenantId) {
    return repository.findByTenantIdOrderByCreatedAtDesc(tenantId).stream()
        .map(this::toDomain)
        .toList();
  }

  private CashTransactionEntity toEntity(CashTransaction transaction) {
    CashTransactionEntity entity = new CashTransactionEntity();
    entity.setId(transaction.id());
    entity.setTenantId(transaction.tenantId());
    entity.setTicketNumber(transaction.ticketNumber());
    entity.setCustomerNumber(transaction.customerNumber());
    entity.setCustomerDisplayName(transaction.customerDisplayName());
    entity.setType(transaction.type());
    entity.setOutstandingLoanAmount(transaction.outstandingLoanAmount());
    entity.setInterestAmount(transaction.interestAmount());
    entity.setOperatingFeeAmount(transaction.operatingFeeAmount());
    entity.setTotalAmount(transaction.totalAmount());
    entity.setLegalText(transaction.legalText());
    entity.setNote(transaction.note());
    entity.setCreatedAt(transaction.createdAt());
    return entity;
  }

  private CashTransaction toDomain(CashTransactionEntity entity) {
    return new CashTransaction(
        entity.getId(),
        entity.getTenantId(),
        entity.getTicketNumber(),
        entity.getCustomerNumber(),
        entity.getCustomerDisplayName(),
        entity.getType(),
        entity.getOutstandingLoanAmount(),
        entity.getInterestAmount(),
        entity.getOperatingFeeAmount(),
        entity.getTotalAmount(),
        entity.getLegalText(),
        entity.getNote(),
        entity.getCreatedAt());
  }
}
