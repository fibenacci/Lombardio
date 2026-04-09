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
package io.lombardio.pawnticket.application.service;

import io.lombardio.pawnticket.domain.model.PawnTicket;
import io.lombardio.pawnticket.domain.model.PawnTicketPosition;
import io.lombardio.pawnticket.domain.port.PawnTicketRepository;
import io.lombardio.platform.security.Audited;
import io.micrometer.core.instrument.MeterRegistry;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PawnTicketPolicyService {

  private static final int DEFAULT_TERM_MONTHS = 3;

  private final PawnTicketRepository pawnTicketRepository;
  private final PawnTicketTermsService termsService;
  private final Clock clock;
  private final PawnTicketMetrics metrics;
  private final AtomicInteger ticketSequence = new AtomicInteger(5000);

  public PawnTicketPolicyService(
      PawnTicketRepository pawnTicketRepository, PawnTicketTermsService termsService, Clock clock) {
    this(pawnTicketRepository, termsService, clock, PawnTicketMetrics.noop());
  }

  @Autowired
  public PawnTicketPolicyService(
      PawnTicketRepository pawnTicketRepository,
      PawnTicketTermsService termsService,
      Clock clock,
      MeterRegistry meterRegistry) {
    this(pawnTicketRepository, termsService, clock, new PawnTicketMetrics(meterRegistry));
  }

  private PawnTicketPolicyService(
      PawnTicketRepository pawnTicketRepository,
      PawnTicketTermsService termsService,
      Clock clock,
      PawnTicketMetrics metrics) {
    this.pawnTicketRepository = pawnTicketRepository;
    this.termsService = termsService;
    this.clock = clock;
    this.metrics = metrics;
  }

  public PawnTicket quote(PawnTicketQuoteCommand command) {
    PawnTicketTermsService.TermsSnapshot termsSnapshot = termsService.currentTerms();
    int normalizedTermMonths =
        command.termMonths() == null
            ? DEFAULT_TERM_MONTHS
            : Math.max(command.termMonths(), DEFAULT_TERM_MONTHS);

    return PawnTicket.createQuote(
        "quote-" + UUID.randomUUID(),
        termsSnapshot.version(),
        termsSnapshot.text(),
        Instant.now(clock),
        LocalDate.now(clock),
        normalizedTermMonths,
        command.loanAmount(),
        command.manualMonthlyOperatingFee());
  }

  @Audited(action = "ISSUE_PAWN_TICKET", targetType = "PAWN_TICKET")
  public PawnTicket issue(IssuePawnTicketCommand command) {
    PawnTicket quote =
        quote(
            new PawnTicketQuoteCommand(
                command.loanAmount(), command.termMonths(), command.manualMonthlyOperatingFee()));
    String contractNumber = "PS-" + ticketSequence.incrementAndGet();
    List<PawnTicketPosition> normalizedPositions =
        normalizePositions(contractNumber, command.positions());

    PawnTicket issued =
        new PawnTicket(
            "ticket-" + UUID.randomUUID(),
            command.tenantId(),
            command.customerId(),
            command.customerNumber(),
            command.customerDisplayName(),
            command.customerPhone(),
            contractNumber,
            contractNumber,
            contractNumber,
            quote.termsVersion(),
            quote.termsAndConditionsText(),
            quote.createdAt(),
            quote.dueDate(),
            quote.earliestAuctionDate(),
            quote.termMonths(),
            quote.loanAmount(),
            quote.monthlyInterestRate(),
            quote.monthlyOperatingFee(),
            quote.manualMonthlyOperatingFeeRequired(),
            quote.totalInterestAmount(),
            quote.totalOperatingFeeAmount(),
            quote.totalRepaymentAmount(),
            quote.legalText(),
            normalizedPositions);

    PawnTicket saved = pawnTicketRepository.save(issued);
    metrics.recordIssued(saved.loanAmount(), saved.positions().size());
    return saved;
  }

  public PawnTicket extend(PawnTicketSettlementCommand command) {
    int normalizedExtensionMonths =
        command.extensionMonths() == null ? 1 : Math.max(command.extensionMonths(), 1);
    return quote(
        new PawnTicketQuoteCommand(
            command.outstandingLoanAmount(),
            normalizedExtensionMonths,
            command.manualMonthlyOperatingFee()));
  }

  public PawnTicketSettlementResult settlePartial(PawnTicketSettlementCommand command) {
    BigDecimal remainingLoanAmount =
        command
            .outstandingLoanAmount()
            .subtract(command.repaymentAmount())
            .setScale(2, RoundingMode.HALF_UP);
    if (remainingLoanAmount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Repayment exceeds outstanding loan amount");
    }

    PawnTicket quote =
        quote(
            new PawnTicketQuoteCommand(
                remainingLoanAmount,
                command.remainingTermMonths() != null
                    ? command.remainingTermMonths()
                    : Integer.valueOf(DEFAULT_TERM_MONTHS),
                command.manualMonthlyOperatingFee()));
    return new PawnTicketSettlementResult(
        remainingLoanAmount,
        quote.totalInterestAmount(),
        quote.totalOperatingFeeAmount(),
        quote.totalRepaymentAmount(),
        PawnTicket.DEFAULT_LEGAL_TEXT);
  }

  public PawnTicketSettlementResult redeem(PawnTicketSettlementCommand command) {
    PawnTicket quote =
        quote(
            new PawnTicketQuoteCommand(
                command.outstandingLoanAmount(),
                command.remainingTermMonths() != null
                    ? command.remainingTermMonths()
                    : Integer.valueOf(DEFAULT_TERM_MONTHS),
                command.manualMonthlyOperatingFee()));
    return new PawnTicketSettlementResult(
        command.outstandingLoanAmount().setScale(2, RoundingMode.HALF_UP),
        quote.totalInterestAmount(),
        quote.totalOperatingFeeAmount(),
        quote.totalRepaymentAmount(),
        PawnTicket.DEFAULT_LEGAL_TEXT);
  }

  public PawnTicket getIssuedTicket(String ticketNumber) {
    return pawnTicketRepository
        .findByTicketNumber(ticketNumber)
        .orElseThrow(() -> new IllegalArgumentException("Pawn ticket not found"));
  }

  public List<PawnTicket> listIssuedTickets(String tenantId) {
    return pawnTicketRepository.findByTenantId(tenantId);
  }

  public List<PawnTicket> listIssuedTickets(String tenantId, String customerId) {
    return pawnTicketRepository.findByTenantIdAndCustomerId(tenantId, customerId);
  }

  private List<PawnTicketPosition> normalizePositions(
      String contractNumber, List<PawnTicketPosition> positions) {
    java.util.List<PawnTicketPosition> normalized = new java.util.ArrayList<>();
    for (int index = 0; index < positions.size(); index++) {
      PawnTicketPosition position = positions.get(index);
      String itemNumber = contractNumber + "-" + String.format("%02d", index + 1);
      normalized.add(
          new PawnTicketPosition(
              itemNumber,
              itemNumber,
              position.label(),
              position.description(),
              position.pledgedValue()));
    }
    return normalized;
  }
}
