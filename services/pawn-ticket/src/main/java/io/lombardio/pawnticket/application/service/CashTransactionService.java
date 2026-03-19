package io.lombardio.pawnticket.application.service;

import io.lombardio.pawnticket.domain.model.CashTransaction;
import io.lombardio.pawnticket.domain.port.CashTransactionRepository;
import io.lombardio.pawnticket.domain.model.PawnTicket;
import io.lombardio.pawnticket.domain.port.PawnTicketRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class CashTransactionService {

    private final PawnTicketRepository pawnTicketRepository;
    private final CashTransactionRepository cashTransactionRepository;
    private final PawnTicketPolicyService pawnTicketPolicyService;
    private final Clock clock;
    private final PawnTicketMetrics metrics;

    public CashTransactionService(
            PawnTicketRepository pawnTicketRepository,
            CashTransactionRepository cashTransactionRepository,
            PawnTicketPolicyService pawnTicketPolicyService,
            Clock clock
    ) {
        this(
                pawnTicketRepository,
                cashTransactionRepository,
                pawnTicketPolicyService,
                clock,
                PawnTicketMetrics.noop()
        );
    }

    @Autowired
    public CashTransactionService(
            PawnTicketRepository pawnTicketRepository,
            CashTransactionRepository cashTransactionRepository,
            PawnTicketPolicyService pawnTicketPolicyService,
            Clock clock,
            MeterRegistry meterRegistry
    ) {
        this(
                pawnTicketRepository,
                cashTransactionRepository,
                pawnTicketPolicyService,
                clock,
                new PawnTicketMetrics(meterRegistry)
        );
    }

    private CashTransactionService(
            PawnTicketRepository pawnTicketRepository,
            CashTransactionRepository cashTransactionRepository,
            PawnTicketPolicyService pawnTicketPolicyService,
            Clock clock,
            PawnTicketMetrics metrics
    ) {
        this.pawnTicketRepository = pawnTicketRepository;
        this.cashTransactionRepository = cashTransactionRepository;
        this.pawnTicketPolicyService = pawnTicketPolicyService;
        this.clock = clock;
        this.metrics = metrics;
    }

    public CashTransaction execute(ExecuteCashTransactionCommand request) {
        PawnTicket pawnTicket = pawnTicketRepository.findByTicketNumber(request.ticketNumber())
                .filter(ticket -> ticket.tenantId().equals(request.tenantId()))
                .orElseThrow(() -> new IllegalArgumentException("Pawn ticket not found"));

        PawnTicketSettlementResult settlement = switch (request.type()) {
            case EXTEND -> {
                PawnTicket quote = pawnTicketPolicyService.extend(new PawnTicketSettlementCommand(
                        request.outstandingLoanAmount(),
                        null,
                        null,
                        request.extensionMonths(),
                        request.manualMonthlyOperatingFee()
                ));
                yield new PawnTicketSettlementResult(
                        quote.loanAmount(),
                        quote.totalInterestAmount(),
                        quote.totalOperatingFeeAmount(),
                        quote.totalRepaymentAmount(),
                        quote.legalText()
                );
            }
            case PARTIAL_REPAYMENT -> pawnTicketPolicyService.settlePartial(new PawnTicketSettlementCommand(
                    request.outstandingLoanAmount(),
                    request.repaymentAmount(),
                    request.remainingTermMonths(),
                    null,
                    request.manualMonthlyOperatingFee()
            ));
            case REDEEM -> pawnTicketPolicyService.redeem(new PawnTicketSettlementCommand(
                    request.outstandingLoanAmount(),
                    null,
                    request.remainingTermMonths(),
                    null,
                    request.manualMonthlyOperatingFee()
            ));
        };

        CashTransaction transaction = new CashTransaction(
                "cash-" + UUID.randomUUID(),
                request.tenantId(),
                pawnTicket.ticketNumber(),
                pawnTicket.customerNumber(),
                pawnTicket.customerDisplayName(),
                request.type(),
                settlement.outstandingLoanAmount(),
                settlement.interestAmount(),
                settlement.operatingFeeAmount(),
                settlement.totalDueAmount(),
                settlement.legalText(),
                request.note(),
                Instant.now(clock)
        );

        CashTransaction saved = cashTransactionRepository.save(transaction);
        metrics.recordCashTransaction(saved.type(), saved.totalAmount());
        return saved;
    }

    public List<CashTransaction> listTransactions(String tenantId) {
        return cashTransactionRepository.findByTenantId(tenantId);
    }
}
