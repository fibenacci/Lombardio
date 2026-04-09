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
package io.lombardio.loanorigination.application.service;

import io.lombardio.loanorigination.domain.model.CustomerProfile;
import io.lombardio.loanorigination.domain.model.LoanCase;
import io.lombardio.loanorigination.domain.model.LoanPosition;
import io.lombardio.loanorigination.domain.model.PawnTicket;
import io.lombardio.loanorigination.domain.model.PledgeRecord;
import io.lombardio.loanorigination.domain.model.ValuationGuideline;
import io.lombardio.loanorigination.domain.port.AmlDirectory;
import io.lombardio.loanorigination.domain.port.CustomerDirectory;
import io.lombardio.loanorigination.domain.port.KycDirectory;
import io.lombardio.loanorigination.domain.port.LoanCaseRepository;
import io.lombardio.loanorigination.domain.port.PawnTicketIssuer;
import io.lombardio.loanorigination.domain.port.ValuationGuidelineRepository;
import io.lombardio.platform.security.Audited;
import io.micrometer.core.instrument.MeterRegistry;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoanOriginationService {

  private final CustomerDirectory customerDirectory;
  private final KycDirectory kycDirectory;
  private final AmlDirectory amlDirectory;
  private final ValuationGuidelineRepository valuationGuidelineRepository;
  private final LoanCaseRepository loanCaseRepository;
  private final PawnTicketIssuer pawnTicketIssuer;
  private final Clock clock;
  private final LoanOriginationMetrics metrics;

  public LoanOriginationService(
      CustomerDirectory customerDirectory,
      KycDirectory kycDirectory,
      AmlDirectory amlDirectory,
      ValuationGuidelineRepository valuationGuidelineRepository,
      LoanCaseRepository loanCaseRepository,
      PawnTicketIssuer pawnTicketIssuer,
      Clock clock,
      @Autowired(required = false) MeterRegistry meterRegistry) {
    this.customerDirectory = customerDirectory;
    this.kycDirectory = kycDirectory;
    this.amlDirectory = amlDirectory;
    this.valuationGuidelineRepository = valuationGuidelineRepository;
    this.loanCaseRepository = loanCaseRepository;
    this.pawnTicketIssuer = pawnTicketIssuer;
    this.clock = clock;
    this.metrics =
        meterRegistry != null
            ? new LoanOriginationMetrics(meterRegistry)
            : LoanOriginationMetrics.noop();
  }

  public List<ValuationGuideline> listGuidelines(String tenantId) {
    return valuationGuidelineRepository.findByTenantId(tenantId);
  }

  public List<LoanCase> listLoans(String tenantId, String customerId) {
    if (customerId == null || customerId.isBlank()) {
      return loanCaseRepository.findByTenantId(tenantId);
    }
    return loanCaseRepository.findByTenantIdAndCustomerId(tenantId, customerId);
  }

  @Audited(action = "CREATE_LOAN_CASE", targetType = "LOAN_CASE")
  public LoanCase createLoan(String tenantId, CreateLoanCommand request) {
    try {
      CustomerProfile customer = customerDirectory.requireById(tenantId, request.customerId());

      if (!kycDirectory.isApproved(tenantId, request.customerId())) {
        throw new IllegalArgumentException("KYC approval required before loan origination");
      }

      List<LoanPosition> positions =
          request.positions().stream().map(p -> buildPosition(tenantId, p)).toList();

      BigDecimal totalLoanValue =
          positions.stream()
              .map(LoanPosition::pledgedValue)
              .reduce(BigDecimal.ZERO, BigDecimal::add);

      AmlDirectory.AmlAssessment amlAssessment =
          amlDirectory.assessForOrigination(tenantId, request.customerId(), totalLoanValue);
      if (!amlAssessment.originationAllowed()) {
        throw new IllegalArgumentException(amlAssessment.decisionReason());
      }

      List<PawnTicket> pawnTickets =
          positions.stream()
              .collect(java.util.stream.Collectors.groupingBy(LoanPosition::ticketGroup))
              .entrySet()
              .stream()
              .sorted(Map.Entry.comparingByKey())
              .map(entry -> issuePawnTicket(tenantId, customer, entry.getValue(), request))
              .toList();

      PledgeRecord pledgeRecord =
          PledgeRecord.create(null, tenantId, customer, request, Instant.now(clock));
      LoanCase loanCase = LoanCase.create(tenantId, customer, pledgeRecord, positions, pawnTickets);

      // Link pledge record to loan case
      PledgeRecord linkedPledgeRecord =
          new PledgeRecord(
              pledgeRecord.id(),
              loanCase.id(),
              pledgeRecord.tenantId(),
              pledgeRecord.recordedAt(),
              pledgeRecord.languageCode(),
              pledgeRecord.retentionUntil(),
              pledgeRecord.pledgorName(),
              pledgeRecord.pledgorStreet(),
              pledgeRecord.pledgorPostalCode(),
              pledgeRecord.pledgorCity(),
              pledgeRecord.pledgorBirthDate(),
              pledgeRecord.checkedDocumentType(),
              pledgeRecord.powerOfAttorneyRequired(),
              pledgeRecord.bearerName(),
              pledgeRecord.bearerStreet(),
              pledgeRecord.bearerPostalCode(),
              pledgeRecord.bearerCity(),
              pledgeRecord.powerOfAttorneyDocumentDataUrl());

      LoanCase finalizedCase =
          new LoanCase(
              loanCase.id(),
              loanCase.tenantId(),
              loanCase.customer(),
              linkedPledgeRecord,
              loanCase.positions(),
              loanCase.pawnTickets());

      loanCaseRepository.save(finalizedCase);
      metrics.recordCreated(totalLoanValue);

      return finalizedCase;
    } catch (IllegalArgumentException exception) {
      metrics.recordRejected(exception.getMessage());
      throw exception;
    }
  }

  private PawnTicket issuePawnTicket(
      String tenantId,
      CustomerProfile customer,
      List<LoanPosition> ticketPositions,
      CreateLoanCommand request) {
    BigDecimal totalLoanValue =
        ticketPositions.stream()
            .map(LoanPosition::pledgedValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    return pawnTicketIssuer.issue(
        tenantId,
        customer,
        ticketPositions,
        totalLoanValue,
        request.termMonths(),
        request.manualMonthlyOperatingFee());
  }

  private LoanPosition buildPosition(String tenantId, CreateLoanPositionCommand request) {
    ValuationGuideline guideline =
        valuationGuidelineRepository
            .findById(request.guidelineId())
            .filter(item -> item.tenantId().equals(tenantId))
            .orElseThrow(() -> new IllegalArgumentException("Valuation guideline not found"));

    return LoanPosition.create(request, guideline);
  }
}
