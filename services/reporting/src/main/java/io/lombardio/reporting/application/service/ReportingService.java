package io.lombardio.reporting.application.service;

import io.lombardio.reporting.api.http.FinanceSummaryResponse;
import io.lombardio.reporting.api.http.FinanceTrendPointResponse;
import io.lombardio.reporting.api.http.InventoryCategoryResponse;
import io.lombardio.reporting.api.http.ReportingDashboardResponse;
import io.lombardio.reporting.api.http.TransactionMixResponse;
import io.lombardio.reporting.domain.port.LoanReadClient;
import io.lombardio.reporting.domain.port.PawnTicketReadClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportingService {

    private final LoanReadClient loanReadClient;
    private final PawnTicketReadClient pawnTicketReadClient;
    private final Clock clock;

    public ReportingService(
            LoanReadClient loanReadClient,
            PawnTicketReadClient pawnTicketReadClient,
            Clock clock
    ) {
        this.loanReadClient = loanReadClient;
        this.pawnTicketReadClient = pawnTicketReadClient;
        this.clock = clock;
    }

    public ReportingDashboardResponse getDashboard(String tenantId, int rangeDays, String bearerToken) {
        int normalizedRangeDays = Math.max(7, Math.min(rangeDays, 90));
        LocalDate rangeEnd = LocalDate.now(clock);
        LocalDate rangeStart = rangeEnd.minusDays(normalizedRangeDays - 1L);

        List<LoanReadClient.ReportedLoanCase> loans = loanReadClient.listLoans(tenantId, bearerToken);
        List<PawnTicketReadClient.ReportedPawnTicketOverview> tickets = pawnTicketReadClient.listTickets(tenantId, bearerToken);
        List<PawnTicketReadClient.ReportedCashTransaction> cashTransactions = pawnTicketReadClient.listCashTransactions(tenantId, bearerToken);

        BigDecimal cashOutflow = loans.stream()
                .filter(loan -> isWithinRange(loan.recordedAt(), rangeStart, rangeEnd))
                .flatMap(loan -> loan.pawnTickets().stream())
                .map(LoanReadClient.ReportedPawnTicket::totalLoanValue)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal cashInflow = cashTransactions.stream()
                .filter(transaction -> isWithinRange(transaction.createdAt(), rangeStart, rangeEnd))
                .map(PawnTicketReadClient.ReportedCashTransaction::totalAmount)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal realizedRevenue = cashTransactions.stream()
                .filter(transaction -> isWithinRange(transaction.createdAt(), rangeStart, rangeEnd))
                .map(transaction -> zeroIfNull(transaction.interestAmount()).add(zeroIfNull(transaction.operatingFeeAmount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal activeLoanExposure = tickets.stream()
                .map(PawnTicketReadClient.ReportedPawnTicketOverview::totalLoanValue)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int activeTicketCount = tickets.size();
        BigDecimal averageTicketValue = activeTicketCount == 0
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : activeLoanExposure.divide(BigDecimal.valueOf(activeTicketCount), 2, RoundingMode.HALF_UP);

        return new ReportingDashboardResponse(
                rangeStart,
                rangeEnd,
                Instant.now(clock),
                new FinanceSummaryResponse(
                        scale(cashInflow),
                        scale(cashOutflow),
                        scale(cashInflow.subtract(cashOutflow)),
                        scale(realizedRevenue),
                        scale(activeLoanExposure),
                        activeTicketCount,
                        scale(averageTicketValue)
                ),
                buildFinanceTrend(loans, cashTransactions, rangeStart, rangeEnd),
                buildInventoryByCategory(loans),
                buildTransactionMix(cashTransactions, rangeStart, rangeEnd)
        );
    }

    private List<FinanceTrendPointResponse> buildFinanceTrend(
            List<LoanReadClient.ReportedLoanCase> loans,
            List<PawnTicketReadClient.ReportedCashTransaction> cashTransactions,
            LocalDate rangeStart,
            LocalDate rangeEnd
    ) {
        Map<LocalDate, BigDecimal> outflowByDay = new LinkedHashMap<>();
        Map<LocalDate, BigDecimal> inflowByDay = new LinkedHashMap<>();
        Map<LocalDate, BigDecimal> revenueByDay = new LinkedHashMap<>();

        LocalDate day = rangeStart;
        while (!day.isAfter(rangeEnd)) {
            outflowByDay.put(day, BigDecimal.ZERO);
            inflowByDay.put(day, BigDecimal.ZERO);
            revenueByDay.put(day, BigDecimal.ZERO);
            day = day.plusDays(1);
        }

        for (LoanReadClient.ReportedLoanCase loan : loans) {
            LocalDate loanDate = toLocalDate(loan.recordedAt());
            if (loanDate == null || loanDate.isBefore(rangeStart) || loanDate.isAfter(rangeEnd)) {
                continue;
            }

            BigDecimal loanAmount = loan.pawnTickets().stream()
                    .map(LoanReadClient.ReportedPawnTicket::totalLoanValue)
                    .filter(java.util.Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            outflowByDay.computeIfPresent(loanDate, (key, current) -> current.add(loanAmount));
        }

        for (PawnTicketReadClient.ReportedCashTransaction transaction : cashTransactions) {
            LocalDate transactionDate = toLocalDate(transaction.createdAt());
            if (transactionDate == null || transactionDate.isBefore(rangeStart) || transactionDate.isAfter(rangeEnd)) {
                continue;
            }

            inflowByDay.computeIfPresent(transactionDate, (key, current) -> current.add(zeroIfNull(transaction.totalAmount())));
            revenueByDay.computeIfPresent(
                    transactionDate,
                    (key, current) -> current.add(zeroIfNull(transaction.interestAmount())).add(zeroIfNull(transaction.operatingFeeAmount()))
            );
        }

        return outflowByDay.keySet().stream()
                .map(date -> new FinanceTrendPointResponse(
                        date,
                        scale(inflowByDay.get(date)),
                        scale(outflowByDay.get(date)),
                        scale(revenueByDay.get(date))
                ))
                .toList();
    }

    private List<InventoryCategoryResponse> buildInventoryByCategory(List<LoanReadClient.ReportedLoanCase> loans) {
        Map<String, InventoryAggregate> aggregates = new LinkedHashMap<>();

        for (LoanReadClient.ReportedLoanCase loan : loans) {
            for (LoanReadClient.ReportedLoanPosition position : loan.positions()) {
                String category = normalizeCategory(position);
                InventoryAggregate aggregate = aggregates.computeIfAbsent(category, ignored -> new InventoryAggregate());
                aggregate.itemCount += 1;
                aggregate.pledgedValue = aggregate.pledgedValue.add(zeroIfNull(position.pledgedValue()));
            }
        }

        return aggregates.entrySet().stream()
                .map(entry -> new InventoryCategoryResponse(
                        entry.getKey(),
                        entry.getValue().itemCount,
                        scale(entry.getValue().pledgedValue)
                ))
                .sorted(Comparator.comparing(InventoryCategoryResponse::pledgedValue).reversed())
                .limit(6)
                .toList();
    }

    private List<TransactionMixResponse> buildTransactionMix(
            List<PawnTicketReadClient.ReportedCashTransaction> cashTransactions,
            LocalDate rangeStart,
            LocalDate rangeEnd
    ) {
        Map<String, TransactionAggregate> aggregates = new LinkedHashMap<>();

        for (PawnTicketReadClient.ReportedCashTransaction transaction : cashTransactions) {
            if (!isWithinRange(transaction.createdAt(), rangeStart, rangeEnd)) {
                continue;
            }

            String type = transaction.type() == null ? "UNKNOWN" : transaction.type();
            TransactionAggregate aggregate = aggregates.computeIfAbsent(type, ignored -> new TransactionAggregate());
            aggregate.transactionCount += 1;
            aggregate.totalAmount = aggregate.totalAmount.add(zeroIfNull(transaction.totalAmount()));
        }

        return aggregates.entrySet().stream()
                .map(entry -> new TransactionMixResponse(
                        entry.getKey(),
                        entry.getValue().transactionCount,
                        scale(entry.getValue().totalAmount)
                ))
                .sorted(Comparator.comparing(TransactionMixResponse::totalAmount).reversed())
                .toList();
    }

    private boolean isWithinRange(Instant instant, LocalDate rangeStart, LocalDate rangeEnd) {
        LocalDate date = toLocalDate(instant);
        return date != null && !date.isBefore(rangeStart) && !date.isAfter(rangeEnd);
    }

    private LocalDate toLocalDate(Instant instant) {
        return instant == null ? null : instant.atZone(ZoneOffset.UTC).toLocalDate();
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String normalizeCategory(LoanReadClient.ReportedLoanPosition position) {
        if (position.guidelineLabel() != null && !position.guidelineLabel().isBlank()) {
            return position.guidelineLabel();
        }
        if (position.label() != null && !position.label().isBlank()) {
            return position.label();
        }
        return "Unbekannt";
    }

    private static final class InventoryAggregate {
        private int itemCount;
        private BigDecimal pledgedValue = BigDecimal.ZERO;
    }

    private static final class TransactionAggregate {
        private int transactionCount;
        private BigDecimal totalAmount = BigDecimal.ZERO;
    }
}
