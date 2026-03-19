package io.lombardio.pawnticket.demo;

import io.lombardio.pawnticket.domain.model.CashTransaction;
import io.lombardio.pawnticket.domain.model.CashTransactionType;
import io.lombardio.pawnticket.domain.model.PawnTicket;
import io.lombardio.pawnticket.domain.model.PawnTicketPosition;
import io.lombardio.pawnticket.domain.port.CashTransactionRepository;
import io.lombardio.pawnticket.domain.port.PawnTicketRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
class ScenarioDataSeeder {

    private record DemoTenant(String id, String key, String numberPrefix, String city) {
    }

    private static final List<DemoTenant> TENANTS = List.of(
            new DemoTenant("tenant-default", "default", "BER", "Berlin"),
            new DemoTenant("tenant-hamburg", "hanseatic", "HAM", "Hamburg"),
            new DemoTenant("tenant-munich", "isar", "MUC", "Muenchen"),
            new DemoTenant("tenant-cologne", "rhein", "CGN", "Koeln"),
            new DemoTenant("tenant-stuttgart", "neckar", "STR", "Stuttgart")
    );

    private static final String[] FIRST_NAMES = {"Anna", "Murat", "Leonie", "Jonas", "Sofia", "Mila", "Emre", "Paul", "Nina", "David", "Lina", "Felix"};
    private static final String[] LAST_NAMES = {"Becker", "Yilmaz", "Schmidt", "Kaya", "Wagner", "Hartmann", "Keller", "Nguyen", "Fischer", "Ali", "Scholz", "Krause"};

    private final PawnTicketRepository pawnTicketRepository;
    private final CashTransactionRepository cashTransactionRepository;
    private final DemoDataProperties demoDataProperties;

    ScenarioDataSeeder(
            PawnTicketRepository pawnTicketRepository,
            CashTransactionRepository cashTransactionRepository,
            DemoDataProperties demoDataProperties
    ) {
        this.pawnTicketRepository = pawnTicketRepository;
        this.cashTransactionRepository = cashTransactionRepository;
        this.demoDataProperties = demoDataProperties;
    }

    void seed() {
        int tenantCount = tenantCount(demoDataProperties.effectiveScale());
        int ticketsPerTenant = ticketsPerTenant(demoDataProperties.effectiveScale());

        for (int tenantIndex = 0; tenantIndex < tenantCount; tenantIndex++) {
            DemoTenant tenant = TENANTS.get(tenantIndex);
            for (int ticketIndex = 1; ticketIndex <= ticketsPerTenant; ticketIndex++) {
                PawnTicket ticket = buildTicket(tenant, tenantIndex, ticketIndex);
                pawnTicketRepository.save(ticket);
                for (CashTransaction transaction : buildTransactions(ticket, tenant, ticketIndex)) {
                    cashTransactionRepository.save(transaction);
                }
            }
        }
    }

    private PawnTicket buildTicket(DemoTenant tenant, int tenantIndex, int ticketIndex) {
        int customerIndex = ticketIndex;
        String firstName = FIRST_NAMES[(customerIndex + tenantIndex) % FIRST_NAMES.length];
        String lastName = LAST_NAMES[(customerIndex * 2 + tenantIndex) % LAST_NAMES.length];
        String customerNumber = tenant.numberPrefix() + "-" + String.format("%04d", 1000 + customerIndex);
        Instant createdAt = Instant.now().minusSeconds((long) (ticketIndex + tenantIndex * 5) * 86_400L);

        List<PawnTicketPosition> positions = new ArrayList<>();
        positions.add(new PawnTicketPosition(
                "ITEM-" + tenant.numberPrefix() + "-" + String.format("%04d", ticketIndex) + "-1",
                "BAR-" + tenant.numberPrefix() + "-" + String.format("%04d", ticketIndex) + "-1",
                ticketIndex % 2 == 0 ? "Goldring 585" : "Apple iPhone 14",
                ticketIndex % 2 == 0 ? "Gelbgold 14 Karat" : "128GB, guter Zustand",
                ticketIndex % 2 == 0 ? new BigDecimal("180.00") : new BigDecimal("260.00")
        ));
        if (ticketIndex % 4 == 0) {
            positions.add(new PawnTicketPosition(
                    "ITEM-" + tenant.numberPrefix() + "-" + String.format("%04d", ticketIndex) + "-2",
                    "BAR-" + tenant.numberPrefix() + "-" + String.format("%04d", ticketIndex) + "-2",
                    "Vintage Uhr",
                    "Mechanisch, gereinigt",
                    new BigDecimal("240.00")
            ));
        }

        BigDecimal loanAmount = positions.stream().map(PawnTicketPosition::pledgedValue).reduce(BigDecimal.ZERO, BigDecimal::add).multiply(new BigDecimal("0.72")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal monthlyInterestRate = new BigDecimal("0.035");
        BigDecimal monthlyOperatingFee = ticketIndex % 6 == 0 ? new BigDecimal("22.50") : new BigDecimal("15.00");
        BigDecimal totalInterestAmount = loanAmount.multiply(monthlyInterestRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalOperatingFeeAmount = monthlyOperatingFee.multiply(BigDecimal.valueOf(4L)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalRepaymentAmount = loanAmount.add(totalInterestAmount).add(totalOperatingFeeAmount).setScale(2, RoundingMode.HALF_UP);

        return new PawnTicket(
                "pawn-ticket-" + tenant.key() + "-" + String.format("%04d", ticketIndex),
                tenant.id(),
                "customer-" + tenant.key() + "-" + String.format("%04d", customerIndex),
                customerNumber,
                firstName + " " + lastName,
                "+49 1" + String.format("%02d", 50 + tenantIndex) + " " + String.format("%06d", 100000 + customerIndex),
                "VT-" + tenant.numberPrefix() + "-" + String.format("%05d", ticketIndex),
                "VTBC-" + tenant.numberPrefix() + "-" + String.format("%05d", ticketIndex),
                "PS-" + tenant.numberPrefix() + "-" + String.format("%05d", ticketIndex),
                "2026-03",
                "Allgemeine Pfandbedingungen fuer Demo-Daten",
                createdAt,
                LocalDate.now().plusMonths(4).plusDays(ticketIndex % 20),
                LocalDate.now().plusMonths(6).plusDays(ticketIndex % 20),
                4,
                loanAmount,
                monthlyInterestRate,
                monthlyOperatingFee,
                ticketIndex % 6 == 0,
                totalInterestAmount,
                totalOperatingFeeAmount,
                totalRepaymentAmount,
                "Pfandrecht gemaess Demo-Setup fuer " + tenant.city(),
                positions
        );
    }

    private List<CashTransaction> buildTransactions(PawnTicket ticket, DemoTenant tenant, int ticketIndex) {
        List<CashTransaction> transactions = new ArrayList<>();
        Instant base = ticket.createdAt().plusSeconds(86_400L);
        if (ticketIndex % 2 == 0) {
            transactions.add(new CashTransaction(
                    "cash-" + tenant.key() + "-" + String.format("%04d", ticketIndex) + "-extend",
                    tenant.id(),
                    ticket.ticketNumber(),
                    ticket.customerNumber(),
                    ticket.customerDisplayName(),
                    CashTransactionType.EXTEND,
                    ticket.loanAmount(),
                    ticket.totalInterestAmount(),
                    ticket.monthlyOperatingFee(),
                    ticket.totalInterestAmount().add(ticket.monthlyOperatingFee()),
                    "Verlaengerung gemaess Demo-Setup",
                    "Verlaengerung im Servicecenter",
                    base
            ));
        }
        if (ticketIndex % 3 == 0) {
            transactions.add(new CashTransaction(
                    "cash-" + tenant.key() + "-" + String.format("%04d", ticketIndex) + "-partial",
                    tenant.id(),
                    ticket.ticketNumber(),
                    ticket.customerNumber(),
                    ticket.customerDisplayName(),
                    CashTransactionType.PARTIAL_REPAYMENT,
                    ticket.loanAmount().subtract(new BigDecimal("50.00")).max(BigDecimal.ZERO),
                    new BigDecimal("8.50"),
                    new BigDecimal("4.00"),
                    new BigDecimal("62.50"),
                    "Teilrueckzahlung gemaess Demo-Setup",
                    "Teilrueckzahlung am Schalter",
                    base.plusSeconds(172_800L)
            ));
        }
        if (ticketIndex % 5 == 0) {
            transactions.add(new CashTransaction(
                    "cash-" + tenant.key() + "-" + String.format("%04d", ticketIndex) + "-redeem",
                    tenant.id(),
                    ticket.ticketNumber(),
                    ticket.customerNumber(),
                    ticket.customerDisplayName(),
                    CashTransactionType.REDEEM,
                    BigDecimal.ZERO,
                    ticket.totalInterestAmount(),
                    ticket.totalOperatingFeeAmount(),
                    ticket.totalRepaymentAmount(),
                    "Ausloesung gemaess Demo-Setup",
                    "Komplette Ausloesung abgeschlossen",
                    base.plusSeconds(345_600L)
            ));
        }
        return transactions;
    }

    private int tenantCount(String scale) {
        return switch (normalize(scale)) {
            case "small" -> 2;
            case "large" -> TENANTS.size();
            default -> 4;
        };
    }

    private int ticketsPerTenant(String scale) {
        return switch (normalize(scale)) {
            case "small" -> 10;
            case "large" -> 54;
            default -> 24;
        };
    }

    private String normalize(String scale) {
        return scale == null ? "medium" : scale.trim().toLowerCase();
    }
}
