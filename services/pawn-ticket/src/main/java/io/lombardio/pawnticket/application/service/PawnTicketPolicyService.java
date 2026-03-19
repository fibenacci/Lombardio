package io.lombardio.pawnticket.application.service;

import io.lombardio.pawnticket.domain.model.PawnTicket;
import io.lombardio.pawnticket.domain.model.PawnTicketPosition;
import io.lombardio.pawnticket.domain.port.PawnTicketRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class PawnTicketPolicyService {

    private static final BigDecimal MONTHLY_INTEREST_RATE = new BigDecimal("1.00");
    private static final int DEFAULT_TERM_MONTHS = 3;
    private static final String LEGAL_TEXT = "Kostenmodell gemaess PfandlV: monatlicher Zins 1 Prozent nach § 10 Abs. 1 Nr. 1 PfandlV, Betriebsverguetung nach Anlage zu § 10 Abs. 1 Nr. 2 PfandlV bis 300 Euro Darlehensbetrag, Mindestfaelligkeit 3 Monate nach § 5 Abs. 1 PfandlV. Eine Pfandverwertung ist fruehestens einen Monat nach Faelligkeit zulaessig (§ 9 Abs. 1 PfandlV).";

    private final PawnTicketRepository pawnTicketRepository;
    private final PawnTicketTermsService termsService;
    private final Clock clock;
    private final PawnTicketMetrics metrics;
    private final AtomicInteger ticketSequence = new AtomicInteger(5000);

    public PawnTicketPolicyService(PawnTicketRepository pawnTicketRepository, PawnTicketTermsService termsService, Clock clock) {
        this(
                pawnTicketRepository,
                termsService,
                clock,
                PawnTicketMetrics.noop()
        );
    }

    @Autowired
    public PawnTicketPolicyService(PawnTicketRepository pawnTicketRepository,
                                   PawnTicketTermsService termsService,
                                   Clock clock,
                                   MeterRegistry meterRegistry) {
        this(
                pawnTicketRepository,
                termsService,
                clock,
                new PawnTicketMetrics(meterRegistry)
        );
    }

    private PawnTicketPolicyService(PawnTicketRepository pawnTicketRepository,
                                    PawnTicketTermsService termsService,
                                    Clock clock,
                                    PawnTicketMetrics metrics) {
        this.pawnTicketRepository = pawnTicketRepository;
        this.termsService = termsService;
        this.clock = clock;
        this.metrics = metrics;
    }

    public PawnTicket quote(PawnTicketQuoteCommand command) {
        BigDecimal loanAmount = command.loanAmount();
        PawnTicketTermsService.TermsSnapshot termsSnapshot = termsService.currentTerms();
        int normalizedTermMonths = command.termMonths() == null ? DEFAULT_TERM_MONTHS : Math.max(command.termMonths(), DEFAULT_TERM_MONTHS);
        OperatingFeeResult operatingFeeResult = resolveOperatingFee(loanAmount, command.manualMonthlyOperatingFee());
        LocalDate contractDate = LocalDate.now(clock);
        LocalDate dueDate = contractDate.plusMonths(normalizedTermMonths);

        BigDecimal totalInterestAmount = loanAmount
                .multiply(MONTHLY_INTEREST_RATE)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(normalizedTermMonths))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalOperatingFeeAmount = operatingFeeResult.monthlyOperatingFee()
                .multiply(new BigDecimal(normalizedTermMonths))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalRepaymentAmount = loanAmount
                .add(totalInterestAmount)
                .add(totalOperatingFeeAmount)
                .setScale(2, RoundingMode.HALF_UP);

        return new PawnTicket(
                "quote-" + UUID.randomUUID(),
                "quote",
                "quote",
                "QUOTE",
                "Angebot",
                null,
                "QUOTE",
                "QUOTE",
                "PS-" + ticketSequence.get(),
                termsSnapshot.version(),
                termsSnapshot.text(),
                Instant.now(clock),
                dueDate,
                dueDate.plusMonths(1),
                normalizedTermMonths,
                loanAmount.setScale(2, RoundingMode.HALF_UP),
                MONTHLY_INTEREST_RATE,
                operatingFeeResult.monthlyOperatingFee(),
                operatingFeeResult.manualRequired(),
                totalInterestAmount,
                totalOperatingFeeAmount,
                totalRepaymentAmount,
                LEGAL_TEXT,
                List.of(new PawnTicketPosition("QUOTE-01", "QUOTE-01", "Pfandgegenstand", "Angebotsvorschau", loanAmount))
        );
    }

    public PawnTicket issue(IssuePawnTicketCommand command) {
        PawnTicket quote = quote(new PawnTicketQuoteCommand(
                command.loanAmount(),
                command.termMonths(),
                command.manualMonthlyOperatingFee()
        ));
        String contractNumber = "PS-" + ticketSequence.incrementAndGet();
        List<PawnTicketPosition> normalizedPositions = normalizePositions(contractNumber, command.positions());
        PawnTicket issued = new PawnTicket(
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
                normalizedPositions
        );
        PawnTicket saved = pawnTicketRepository.save(issued);
        metrics.recordIssued(saved.loanAmount(), saved.positions().size());
        return saved;
    }

    public PawnTicket extend(PawnTicketSettlementCommand command) {
        int normalizedExtensionMonths = command.extensionMonths() == null ? 1 : Math.max(command.extensionMonths(), 1);
        return quote(new PawnTicketQuoteCommand(
                command.outstandingLoanAmount(),
                normalizedExtensionMonths,
                command.manualMonthlyOperatingFee()
        ));
    }

    public PawnTicketSettlementResult settlePartial(PawnTicketSettlementCommand command) {
        BigDecimal remainingLoanAmount = command.outstandingLoanAmount().subtract(command.repaymentAmount()).setScale(2, RoundingMode.HALF_UP);
        if (remainingLoanAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Repayment exceeds outstanding loan amount");
        }

        PawnTicket quote = quote(new PawnTicketQuoteCommand(
                remainingLoanAmount,
                command.remainingTermMonths() == null ? DEFAULT_TERM_MONTHS : command.remainingTermMonths(),
                command.manualMonthlyOperatingFee()
        ));
        return new PawnTicketSettlementResult(
                remainingLoanAmount,
                quote.totalInterestAmount(),
                quote.totalOperatingFeeAmount(),
                quote.totalRepaymentAmount(),
                LEGAL_TEXT
        );
    }

    public PawnTicketSettlementResult redeem(PawnTicketSettlementCommand command) {
        PawnTicket quote = quote(new PawnTicketQuoteCommand(
                command.outstandingLoanAmount(),
                command.remainingTermMonths() == null ? DEFAULT_TERM_MONTHS : command.remainingTermMonths(),
                command.manualMonthlyOperatingFee()
        ));
        return new PawnTicketSettlementResult(
                command.outstandingLoanAmount().setScale(2, RoundingMode.HALF_UP),
                quote.totalInterestAmount(),
                quote.totalOperatingFeeAmount(),
                quote.totalRepaymentAmount(),
                LEGAL_TEXT
        );
    }

    public PawnTicket getIssuedTicket(String ticketNumber) {
        return pawnTicketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new IllegalArgumentException("Pawn ticket not found"));
    }

    public List<PawnTicket> listIssuedTickets(String tenantId) {
        return pawnTicketRepository.findByTenantId(tenantId);
    }

    public List<PawnTicket> listIssuedTickets(String tenantId, String customerId) {
        return pawnTicketRepository.findByTenantIdAndCustomerId(tenantId, customerId);
    }

    private List<PawnTicketPosition> normalizePositions(String contractNumber, List<PawnTicketPosition> positions) {
        java.util.List<PawnTicketPosition> normalized = new java.util.ArrayList<>();
        for (int index = 0; index < positions.size(); index++) {
            PawnTicketPosition position = positions.get(index);
            String itemNumber = contractNumber + "-" + String.format("%02d", index + 1);
            normalized.add(new PawnTicketPosition(
                    itemNumber,
                    itemNumber,
                    position.label(),
                    position.description(),
                    position.pledgedValue()
            ));
        }
        return normalized;
    }

    private OperatingFeeResult resolveOperatingFee(BigDecimal loanAmount, BigDecimal manualMonthlyOperatingFee) {
        if (loanAmount.compareTo(new BigDecimal("15.00")) <= 0) {
            return new OperatingFeeResult(new BigDecimal("1.00"), false);
        }
        if (loanAmount.compareTo(new BigDecimal("30.00")) <= 0) {
            return new OperatingFeeResult(new BigDecimal("1.50"), false);
        }
        if (loanAmount.compareTo(new BigDecimal("50.00")) <= 0) {
            return new OperatingFeeResult(new BigDecimal("2.00"), false);
        }
        if (loanAmount.compareTo(new BigDecimal("100.00")) <= 0) {
            return new OperatingFeeResult(new BigDecimal("2.50"), false);
        }
        if (loanAmount.compareTo(new BigDecimal("150.00")) <= 0) {
            return new OperatingFeeResult(new BigDecimal("3.50"), false);
        }
        if (loanAmount.compareTo(new BigDecimal("200.00")) <= 0) {
            return new OperatingFeeResult(new BigDecimal("4.50"), false);
        }
        if (loanAmount.compareTo(new BigDecimal("250.00")) <= 0) {
            return new OperatingFeeResult(new BigDecimal("5.50"), false);
        }
        if (loanAmount.compareTo(new BigDecimal("300.00")) <= 0) {
            return new OperatingFeeResult(new BigDecimal("6.50"), false);
        }

        if (manualMonthlyOperatingFee == null) {
            return new OperatingFeeResult(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), true);
        }

        return new OperatingFeeResult(manualMonthlyOperatingFee.setScale(2, RoundingMode.HALF_UP), false);
    }

    private record OperatingFeeResult(BigDecimal monthlyOperatingFee, boolean manualRequired) {
    }
}
