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
import io.lombardio.pawnticket.domain.model.PawnTicketPosition;
import io.lombardio.pawnticket.domain.port.PawnTicketRepository;
import io.lombardio.pawnticket.infrastructure.persistence.entity.PawnTicketEntity;
import io.lombardio.pawnticket.infrastructure.persistence.entity.PawnTicketPositionEntity;
import io.lombardio.pawnticket.infrastructure.persistence.repository.SpringDataPawnTicketRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class PawnTicketPersistenceAdapter implements PawnTicketRepository {

  private final SpringDataPawnTicketRepository repository;

  public PawnTicketPersistenceAdapter(SpringDataPawnTicketRepository repository) {
    this.repository = repository;
  }

  @Override
  public PawnTicket save(PawnTicket pawnTicket) {
    return toDomain(repository.save(toEntity(pawnTicket)));
  }

  @Override
  public Optional<PawnTicket> findByTicketNumber(String ticketNumber) {
    return repository.findByTicketNumber(ticketNumber).map(this::toDomain);
  }

  @Override
  public List<PawnTicket> findByTenantId(String tenantId) {
    return repository.findByTenantIdOrderByCreatedAtDesc(tenantId).stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public List<PawnTicket> findByTenantIdAndCustomerId(String tenantId, String customerId) {
    return repository.findByTenantIdAndCustomerIdOrderByCreatedAtDesc(tenantId, customerId).stream()
        .map(this::toDomain)
        .toList();
  }

  private PawnTicketEntity toEntity(PawnTicket pawnTicket) {
    PawnTicketEntity entity = new PawnTicketEntity();
    entity.setId(pawnTicket.id());
    entity.setTenantId(pawnTicket.tenantId());
    entity.setCustomerId(pawnTicket.customerId());
    entity.setCustomerNumber(pawnTicket.customerNumber());
    entity.setCustomerDisplayName(pawnTicket.customerDisplayName());
    entity.setCustomerPhone(pawnTicket.customerPhone());
    entity.setContractNumber(pawnTicket.contractNumber());
    entity.setContractBarcode(pawnTicket.contractBarcode());
    entity.setTicketNumber(pawnTicket.ticketNumber());
    entity.setTermsVersion(pawnTicket.termsVersion());
    entity.setTermsAndConditionsText(pawnTicket.termsAndConditionsText());
    entity.setCreatedAt(pawnTicket.createdAt());
    entity.setDueDate(pawnTicket.dueDate());
    entity.setEarliestAuctionDate(pawnTicket.earliestAuctionDate());
    entity.setTermMonths(pawnTicket.termMonths());
    entity.setLoanAmount(pawnTicket.loanAmount());
    entity.setMonthlyInterestRate(pawnTicket.monthlyInterestRate());
    entity.setMonthlyOperatingFee(pawnTicket.monthlyOperatingFee());
    entity.setManualMonthlyOperatingFeeRequired(pawnTicket.manualMonthlyOperatingFeeRequired());
    entity.setTotalInterestAmount(pawnTicket.totalInterestAmount());
    entity.setTotalOperatingFeeAmount(pawnTicket.totalOperatingFeeAmount());
    entity.setTotalRepaymentAmount(pawnTicket.totalRepaymentAmount());
    entity.setLegalText(pawnTicket.legalText());

    List<PawnTicketPositionEntity> positions = new java.util.ArrayList<>();
    for (int index = 0; index < pawnTicket.positions().size(); index++) {
      positions.add(toPositionEntity(entity, pawnTicket.positions().get(index), index));
    }
    entity.setPositions(new java.util.ArrayList<>(positions));

    return entity;
  }

  private PawnTicketPositionEntity toPositionEntity(
      PawnTicketEntity pawnTicket, PawnTicketPosition position, int sortOrder) {
    PawnTicketPositionEntity entity = new PawnTicketPositionEntity();
    entity.setId(java.util.UUID.randomUUID().toString());
    entity.setPawnTicket(pawnTicket);
    entity.setItemNumber(position.itemNumber());
    entity.setItemBarcode(position.itemBarcode());
    entity.setLabel(position.label());
    entity.setDescription(position.description());
    entity.setPledgedValue(position.pledgedValue());
    entity.setSortOrder(sortOrder);
    return entity;
  }

  private PawnTicket toDomain(PawnTicketEntity entity) {
    return new PawnTicket(
        entity.getId(),
        entity.getTenantId(),
        entity.getCustomerId(),
        entity.getCustomerNumber(),
        entity.getCustomerDisplayName(),
        entity.getCustomerPhone(),
        entity.getContractNumber(),
        entity.getContractBarcode(),
        entity.getTicketNumber(),
        entity.getTermsVersion(),
        entity.getTermsAndConditionsText(),
        entity.getCreatedAt(),
        entity.getDueDate(),
        entity.getEarliestAuctionDate(),
        entity.getTermMonths(),
        entity.getLoanAmount(),
        entity.getMonthlyInterestRate(),
        entity.getMonthlyOperatingFee(),
        entity.isManualMonthlyOperatingFeeRequired(),
        entity.getTotalInterestAmount(),
        entity.getTotalOperatingFeeAmount(),
        entity.getTotalRepaymentAmount(),
        entity.getLegalText(),
        entity.getPositions().stream()
            .map(
                position ->
                    new PawnTicketPosition(
                        position.getItemNumber(),
                        position.getItemBarcode(),
                        position.getLabel(),
                        position.getDescription(),
                        position.getPledgedValue()))
            .toList());
  }
}
