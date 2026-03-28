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
import io.micrometer.core.instrument.MeterRegistry;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
      Clock clock) {
    this(
        customerDirectory,
        kycDirectory,
        amlDirectory,
        valuationGuidelineRepository,
        loanCaseRepository,
        pawnTicketIssuer,
        clock,
        LoanOriginationMetrics.noop());
  }

  @Autowired
  public LoanOriginationService(
      CustomerDirectory customerDirectory,
      KycDirectory kycDirectory,
      AmlDirectory amlDirectory,
      ValuationGuidelineRepository valuationGuidelineRepository,
      LoanCaseRepository loanCaseRepository,
      PawnTicketIssuer pawnTicketIssuer,
      Clock clock,
      MeterRegistry meterRegistry) {
    this(
        customerDirectory,
        kycDirectory,
        amlDirectory,
        valuationGuidelineRepository,
        loanCaseRepository,
        pawnTicketIssuer,
        clock,
        new LoanOriginationMetrics(meterRegistry));
  }

  private LoanOriginationService(
      CustomerDirectory customerDirectory,
      KycDirectory kycDirectory,
      AmlDirectory amlDirectory,
      ValuationGuidelineRepository valuationGuidelineRepository,
      LoanCaseRepository loanCaseRepository,
      PawnTicketIssuer pawnTicketIssuer,
      Clock clock,
      LoanOriginationMetrics metrics) {
    this.customerDirectory = customerDirectory;
    this.kycDirectory = kycDirectory;
    this.amlDirectory = amlDirectory;
    this.valuationGuidelineRepository = valuationGuidelineRepository;
    this.loanCaseRepository = loanCaseRepository;
    this.pawnTicketIssuer = pawnTicketIssuer;
    this.clock = clock;
    this.metrics = metrics;
  }

  public List<ValuationGuideline> listGuidelines(String tenantId) {
    return valuationGuidelineRepository.findByTenantId(tenantId);
  }

  public List<LoanCase> listLoans(String tenantId, String customerId) {
    List<LoanCase> loans =
        customerId == null || customerId.isBlank()
            ? loanCaseRepository.findByTenantId(tenantId)
            : loanCaseRepository.findByTenantIdAndCustomerId(tenantId, customerId);

    return loans;
  }

  public LoanCase createLoan(String tenantId, CreateLoanCommand request) {
    try {
      CustomerProfile customer = customerDirectory.requireById(tenantId, request.customerId());
      if (request.thirdPartyPledgorPresentation()) {
        if (request.bearerName() == null || request.bearerName().isBlank()) {
          throw new IllegalArgumentException("bearerName is required for third-party presentation");
        }
        if (request.powerOfAttorneyDocumentDataUrl() == null
            || request.powerOfAttorneyDocumentDataUrl().isBlank()) {
          throw new IllegalArgumentException(
              "powerOfAttorneyDocumentDataUrl is required for third-party presentation");
        }
      }
      if (!kycDirectory.isApproved(tenantId, request.customerId())) {
        throw new IllegalArgumentException("KYC approval required before loan origination");
      }
      List<LoanPosition> positions =
          request.positions().stream().map(position -> buildPosition(tenantId, position)).toList();
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

      String loanCaseId = "loan-" + UUID.randomUUID();
      PledgeRecord pledgeRecord = buildPledgeRecord(loanCaseId, tenantId, customer, request);

      LoanCase loanCase =
          new LoanCase(loanCaseId, tenantId, customer, pledgeRecord, positions, pawnTickets);
      loanCaseRepository.save(loanCase);
      metrics.recordCreated(totalLoanValue);

      return loanCase;
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

    BigDecimal pledgedValue =
        request.pledgedValue() != null ? request.pledgedValue() : guideline.baseLoanValue();

    return new LoanPosition(
        "position-" + UUID.randomUUID(),
        request.ticketGroup(),
        request.label(),
        request.description(),
        guideline.id(),
        guideline.label(),
        guideline.baseLoanValue(),
        pledgedValue);
  }

  private PledgeRecord buildPledgeRecord(
      String loanCaseId, String tenantId, CustomerProfile customer, CreateLoanCommand request) {
    Instant recordedAt = Instant.now(clock);
    return new PledgeRecord(
        "pledge-" + UUID.randomUUID(),
        loanCaseId,
        tenantId,
        recordedAt,
        "de",
        recordedAt.atZone(clock.getZone()).toLocalDate().plusYears(4),
        customer.displayName(),
        customer.street(),
        customer.postalCode(),
        customer.city(),
        customer.birthDate(),
        customer.checkedDocumentType(),
        request.thirdPartyPledgorPresentation(),
        request.thirdPartyPledgorPresentation() ? request.bearerName() : null,
        request.thirdPartyPledgorPresentation() ? request.bearerStreet() : null,
        request.thirdPartyPledgorPresentation() ? request.bearerPostalCode() : null,
        request.thirdPartyPledgorPresentation() ? request.bearerCity() : null,
        request.thirdPartyPledgorPresentation() ? request.powerOfAttorneyDocumentDataUrl() : null);
  }
}
