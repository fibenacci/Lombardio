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

import io.lombardio.loanorigination.domain.model.CustomerProfile;
import io.lombardio.loanorigination.domain.model.LoanCase;
import io.lombardio.loanorigination.domain.model.LoanPosition;
import io.lombardio.loanorigination.domain.model.PawnTicket;
import io.lombardio.loanorigination.domain.model.PledgeRecord;
import io.lombardio.loanorigination.domain.port.LoanCaseRepository;
import io.lombardio.loanorigination.infrastructure.persistence.entity.LoanCaseEntity;
import io.lombardio.loanorigination.infrastructure.persistence.entity.LoanPawnTicketEntity;
import io.lombardio.loanorigination.infrastructure.persistence.entity.LoanPositionEntity;
import io.lombardio.loanorigination.infrastructure.persistence.entity.PledgeRecordEntity;
import io.lombardio.loanorigination.infrastructure.persistence.repository.SpringDataLoanCaseRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class LoanCasePersistenceAdapter implements LoanCaseRepository {

  private final SpringDataLoanCaseRepository repository;

  public LoanCasePersistenceAdapter(SpringDataLoanCaseRepository repository) {
    this.repository = repository;
  }

  @Override
  public LoanCase save(LoanCase loanCase) {
    return toDomain(repository.save(toEntity(loanCase)));
  }

  @Override
  public List<LoanCase> findByTenantId(String tenantId) {
    return repository.findByTenantId(tenantId).stream()
        .map(this::toDomain)
        .sorted(
            Comparator.comparing((LoanCase loanCase) -> loanCase.pledgeRecord().recordedAt())
                .reversed())
        .toList();
  }

  @Override
  public List<LoanCase> findByTenantIdAndCustomerId(String tenantId, String customerId) {
    return repository.findByTenantIdAndCustomerId(tenantId, customerId).stream()
        .map(this::toDomain)
        .sorted(
            Comparator.comparing((LoanCase loanCase) -> loanCase.pledgeRecord().recordedAt())
                .reversed())
        .toList();
  }

  private LoanCaseEntity toEntity(LoanCase loanCase) {
    LoanCaseEntity entity = new LoanCaseEntity();
    entity.setId(loanCase.id());
    entity.setTenantId(loanCase.tenantId());
    entity.setCustomerId(loanCase.customer().id());
    entity.setCustomerNumber(loanCase.customer().customerNumber());
    entity.setCustomerDisplayName(loanCase.customer().displayName());
    entity.setCustomerBirthDate(loanCase.customer().birthDate());
    entity.setCustomerPhone(loanCase.customer().phone());
    entity.setCustomerStreet(loanCase.customer().street());
    entity.setCustomerPostalCode(loanCase.customer().postalCode());
    entity.setCustomerCity(loanCase.customer().city());
    entity.setCustomerKycStatus(loanCase.customer().kycStatus());
    entity.setCustomerKycApproved(loanCase.customer().kycApproved());
    entity.setCustomerCheckedDocumentType(loanCase.customer().checkedDocumentType());

    PledgeRecordEntity pledgeRecordEntity =
        toPledgeRecordEntity(entity, loanCase.pledgeRecord(), 0);
    entity.setPledgeRecords(new ArrayList<>(List.of(pledgeRecordEntity)));

    List<LoanPositionEntity> positionEntities = new ArrayList<>();
    for (int index = 0; index < loanCase.positions().size(); index++) {
      positionEntities.add(toPositionEntity(entity, loanCase.positions().get(index), index));
    }
    entity.setPositions(new ArrayList<>(positionEntities));

    List<LoanPawnTicketEntity> pawnTicketEntities = new ArrayList<>();
    for (int index = 0; index < loanCase.pawnTickets().size(); index++) {
      pawnTicketEntities.add(toPawnTicketEntity(entity, loanCase.pawnTickets().get(index), index));
    }
    entity.setPawnTickets(new ArrayList<>(pawnTicketEntities));

    return entity;
  }

  private PledgeRecordEntity toPledgeRecordEntity(
      LoanCaseEntity loanCase, PledgeRecord pledgeRecord, int sortOrder) {
    PledgeRecordEntity entity = new PledgeRecordEntity();
    entity.setId(pledgeRecord.id());
    entity.setLoanCase(loanCase);
    entity.setTenantId(pledgeRecord.tenantId());
    entity.setRecordedAt(pledgeRecord.recordedAt());
    entity.setLanguageCode(pledgeRecord.languageCode());
    entity.setRetentionUntil(pledgeRecord.retentionUntil());
    entity.setPledgorName(pledgeRecord.pledgorName());
    entity.setPledgorStreet(pledgeRecord.pledgorStreet());
    entity.setPledgorPostalCode(pledgeRecord.pledgorPostalCode());
    entity.setPledgorCity(pledgeRecord.pledgorCity());
    entity.setPledgorBirthDate(pledgeRecord.pledgorBirthDate());
    entity.setCheckedDocumentType(pledgeRecord.checkedDocumentType());
    entity.setPowerOfAttorneyRequired(pledgeRecord.powerOfAttorneyRequired());
    entity.setBearerName(pledgeRecord.bearerName());
    entity.setBearerStreet(pledgeRecord.bearerStreet());
    entity.setBearerPostalCode(pledgeRecord.bearerPostalCode());
    entity.setBearerCity(pledgeRecord.bearerCity());
    entity.setPowerOfAttorneyDocumentDataUrl(pledgeRecord.powerOfAttorneyDocumentDataUrl());
    entity.setSortOrder(sortOrder);
    return entity;
  }

  private LoanPositionEntity toPositionEntity(
      LoanCaseEntity loanCase, LoanPosition position, int sortOrder) {
    LoanPositionEntity entity = new LoanPositionEntity();
    entity.setId(position.id());
    entity.setLoanCase(loanCase);
    entity.setTicketGroup(position.ticketGroup());
    entity.setLabel(position.label());
    entity.setDescription(position.description());
    entity.setGuidelineId(position.guidelineId());
    entity.setGuidelineLabel(position.guidelineLabel());
    entity.setBaseLoanValue(position.baseLoanValue());
    entity.setPledgedValue(position.pledgedValue());
    entity.setSortOrder(sortOrder);
    return entity;
  }

  private LoanPawnTicketEntity toPawnTicketEntity(
      LoanCaseEntity loanCase, PawnTicket ticket, int sortOrder) {
    LoanPawnTicketEntity entity = new LoanPawnTicketEntity();
    entity.setId(ticket.id());
    entity.setLoanCase(loanCase);
    entity.setContractNumber(ticket.contractNumber());
    entity.setContractBarcode(ticket.contractBarcode());
    entity.setTicketNumber(ticket.ticketNumber());
    entity.setTermsVersion(ticket.termsVersion());
    entity.setTermsAndConditionsText(ticket.termsAndConditionsText());
    entity.setCreatedAt(ticket.createdAt());
    entity.setDueDate(ticket.dueDate());
    entity.setEarliestAuctionDate(ticket.earliestAuctionDate());
    entity.setTermMonths(ticket.termMonths());
    entity.setTotalLoanValue(ticket.totalLoanValue());
    entity.setMonthlyInterestRate(ticket.monthlyInterestRate());
    entity.setMonthlyOperatingFee(ticket.monthlyOperatingFee());
    entity.setManualMonthlyOperatingFeeRequired(ticket.manualMonthlyOperatingFeeRequired());
    entity.setTotalInterestAmount(ticket.totalInterestAmount());
    entity.setTotalOperatingFeeAmount(ticket.totalOperatingFeeAmount());
    entity.setTotalRepaymentAmount(ticket.totalRepaymentAmount());
    entity.setLegalText(ticket.legalText());
    entity.setSortOrder(sortOrder);
    return entity;
  }

  private LoanCase toDomain(LoanCaseEntity entity) {
    return new LoanCase(
        entity.getId(),
        entity.getTenantId(),
        new CustomerProfile(
            entity.getCustomerId(),
            entity.getTenantId(),
            entity.getCustomerNumber(),
            entity.getCustomerDisplayName(),
            entity.getCustomerBirthDate(),
            entity.getCustomerPhone(),
            entity.getCustomerStreet(),
            entity.getCustomerPostalCode(),
            entity.getCustomerCity(),
            entity.getCustomerKycStatus(),
            entity.isCustomerKycApproved(),
            entity.getCustomerCheckedDocumentType()),
        entity.getPledgeRecords().stream()
            .sorted(Comparator.comparing(PledgeRecordEntity::getSortOrder))
            .findFirst()
            .map(
                pledgeRecord ->
                    new PledgeRecord(
                        pledgeRecord.getId(),
                        entity.getId(),
                        pledgeRecord.getTenantId(),
                        pledgeRecord.getRecordedAt(),
                        pledgeRecord.getLanguageCode(),
                        pledgeRecord.getRetentionUntil(),
                        pledgeRecord.getPledgorName(),
                        pledgeRecord.getPledgorStreet(),
                        pledgeRecord.getPledgorPostalCode(),
                        pledgeRecord.getPledgorCity(),
                        pledgeRecord.getPledgorBirthDate(),
                        pledgeRecord.getCheckedDocumentType(),
                        pledgeRecord.isPowerOfAttorneyRequired(),
                        pledgeRecord.getBearerName(),
                        pledgeRecord.getBearerStreet(),
                        pledgeRecord.getBearerPostalCode(),
                        pledgeRecord.getBearerCity(),
                        pledgeRecord.getPowerOfAttorneyDocumentDataUrl()))
            .orElse(null),
        entity.getPositions().stream()
            .sorted(Comparator.comparing(LoanPositionEntity::getSortOrder))
            .map(
                position ->
                    new LoanPosition(
                        position.getId(),
                        position.getTicketGroup(),
                        position.getLabel(),
                        position.getDescription(),
                        position.getGuidelineId(),
                        position.getGuidelineLabel(),
                        position.getBaseLoanValue(),
                        position.getPledgedValue()))
            .toList(),
        entity.getPawnTickets().stream()
            .sorted(Comparator.comparing(LoanPawnTicketEntity::getSortOrder))
            .map(
                ticket ->
                    new PawnTicket(
                        ticket.getId(),
                        ticket.getContractNumber(),
                        ticket.getContractBarcode(),
                        ticket.getTicketNumber(),
                        ticket.getTermsVersion(),
                        ticket.getTermsAndConditionsText(),
                        ticket.getCreatedAt(),
                        ticket.getDueDate(),
                        ticket.getEarliestAuctionDate(),
                        ticket.getTermMonths(),
                        ticket.getTotalLoanValue(),
                        ticket.getMonthlyInterestRate(),
                        ticket.getMonthlyOperatingFee(),
                        ticket.isManualMonthlyOperatingFeeRequired(),
                        ticket.getTotalInterestAmount(),
                        ticket.getTotalOperatingFeeAmount(),
                        ticket.getTotalRepaymentAmount(),
                        ticket.getLegalText(),
                        java.util.List.of()))
            .toList());
  }
}
