package io.lombardio.loanorigination.demo;

import io.lombardio.loanorigination.domain.model.ValuationGuideline;
import io.lombardio.loanorigination.domain.model.CustomerProfile;
import io.lombardio.loanorigination.domain.model.LoanCase;
import io.lombardio.loanorigination.domain.model.LoanPosition;
import io.lombardio.loanorigination.domain.model.PawnTicket;
import io.lombardio.loanorigination.domain.model.PawnTicketPosition;
import io.lombardio.loanorigination.domain.model.PledgeRecord;
import io.lombardio.loanorigination.infrastructure.persistence.adapter.LoanCasePersistenceAdapter;
import io.lombardio.loanorigination.domain.port.ValuationGuidelineRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
class ScenarioDataSeeder {

    private static final String[] FIRST_NAMES = {"Anna", "Murat", "Leonie", "Jonas", "Sofia", "Mila", "Emre", "Paul", "Nina", "David", "Lina", "Felix", "Aylin", "Noah", "Mara", "Yusuf"};
    private static final String[] LAST_NAMES = {"Becker", "Yilmaz", "Schmidt", "Kaya", "Wagner", "Hartmann", "Keller", "Nguyen", "Fischer", "Ali", "Scholz", "Krause", "Demir", "Walter", "Schuster", "Brandt"};
    private static final String[] STREETS = {"Hauptstrasse", "Marktstrasse", "Bergweg", "Lindenallee", "Bahnhofstrasse", "Parkring", "Feldweg", "Muehlenstrasse"};

    private final LoanCasePersistenceAdapter loanCaseRepository;
    private final ValuationGuidelineRepository valuationGuidelineRepository;
    private final DemoDataProperties demoDataProperties;

    ScenarioDataSeeder(
            LoanCasePersistenceAdapter loanCaseRepository,
            ValuationGuidelineRepository valuationGuidelineRepository,
            DemoDataProperties demoDataProperties
    ) {
        this.loanCaseRepository = loanCaseRepository;
        this.valuationGuidelineRepository = valuationGuidelineRepository;
        this.demoDataProperties = demoDataProperties;
    }

    void seed() {
        int tenantCount = tenantCount(demoDataProperties.effectiveScale());
        int casesPerTenant = casesPerTenant(demoDataProperties.effectiveScale());

        for (int tenantIndex = 0; tenantIndex < tenantCount; tenantIndex++) {
            ReferenceDataSeeder.DemoTenant tenant = ReferenceDataSeeder.TENANTS.get(tenantIndex);
            List<ValuationGuideline> guidelines = valuationGuidelineRepository.findByTenantId(tenant.id());
            for (int caseIndex = 1; caseIndex <= casesPerTenant; caseIndex++) {
                loanCaseRepository.save(buildLoanCase(tenant, tenantIndex, caseIndex, guidelines));
            }
        }
    }

    private LoanCase buildLoanCase(ReferenceDataSeeder.DemoTenant tenant, int tenantIndex, int caseIndex, List<ValuationGuideline> guidelines) {
        int customerIndex = caseIndex;
        String customerId = "customer-" + tenant.key() + "-" + String.format("%04d", customerIndex);
        String customerNumber = tenant.numberPrefix() + "-" + String.format("%04d", 1000 + customerIndex);
        String firstName = FIRST_NAMES[(customerIndex + tenantIndex) % FIRST_NAMES.length];
        String lastName = LAST_NAMES[(customerIndex * 2 + tenantIndex) % LAST_NAMES.length];
        Instant recordedAt = Instant.now().minusSeconds((long) (caseIndex + tenantIndex * 9) * 86_400L);

        List<LoanPosition> positions = new ArrayList<>();
        int positionCount = caseIndex % 3 == 0 ? 2 : 1;
        BigDecimal totalLoanValue = BigDecimal.ZERO;
        for (int positionIndex = 0; positionIndex < positionCount; positionIndex++) {
            ValuationGuideline guideline = guidelines.get((caseIndex + positionIndex) % guidelines.size());
            BigDecimal baseLoanValue = guideline.baseLoanValue().add(BigDecimal.valueOf(positionIndex * 35L));
            BigDecimal pledgedValue = baseLoanValue.add(new BigDecimal("55.00"));
            totalLoanValue = totalLoanValue.add(baseLoanValue);
            positions.add(new LoanPosition(
                    "loan-position-" + tenant.key() + "-" + String.format("%04d", caseIndex) + "-" + (positionIndex + 1),
                    positionIndex + 1,
                    guideline.label(),
                    guideline.description(),
                    guideline.id(),
                    guideline.label(),
                    baseLoanValue,
                    pledgedValue
            ));
        }

        BigDecimal monthlyInterestRate = new BigDecimal("0.035");
        BigDecimal monthlyOperatingFee = new BigDecimal(caseIndex % 7 == 0 ? "22.50" : "15.00");
        BigDecimal totalInterestAmount = totalLoanValue.multiply(monthlyInterestRate).setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal totalOperatingFeeAmount = monthlyOperatingFee.multiply(BigDecimal.valueOf(4L)).setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal totalRepaymentAmount = totalLoanValue.add(totalInterestAmount).add(totalOperatingFeeAmount).setScale(2, java.math.RoundingMode.HALF_UP);

        return new LoanCase(
                "loan-case-" + tenant.key() + "-" + String.format("%04d", caseIndex),
                tenant.id(),
                new CustomerProfile(
                        customerId,
                        tenant.id(),
                        customerNumber,
                        firstName + " " + lastName,
                        LocalDate.of(1965 + ((customerIndex + tenantIndex) % 35), ((customerIndex - 1) % 12) + 1, ((customerIndex - 1) % 27) + 1),
                        "+49 1" + String.format("%02d", 50 + tenantIndex) + " " + String.format("%06d", 100000 + customerIndex),
                        STREETS[(customerIndex + tenantIndex) % STREETS.length] + " " + (10 + customerIndex),
                        tenant.postalCode(),
                        tenant.city(),
                        customerIndex % 8 == 2 ? "REJECTED" : (customerIndex % 3 == 0 ? "IN_PROGRESS" : "APPROVED"),
                        customerIndex % 8 != 2,
                        customerIndex % 5 == 0 ? "REISEPASS" : "PERSONALAUSWEIS"
                ),
                new PledgeRecord(
                        "pledge-record-" + tenant.key() + "-" + String.format("%04d", caseIndex),
                        "loan-case-" + tenant.key() + "-" + String.format("%04d", caseIndex),
                        tenant.id(),
                        recordedAt,
                        "de",
                        LocalDate.now().plusYears(10),
                        firstName + " " + lastName,
                        STREETS[(customerIndex + tenantIndex) % STREETS.length] + " " + (10 + customerIndex),
                        tenant.postalCode(),
                        tenant.city(),
                        LocalDate.of(1965 + ((customerIndex + tenantIndex) % 35), ((customerIndex - 1) % 12) + 1, ((customerIndex - 1) % 27) + 1),
                        customerIndex % 5 == 0 ? "REISEPASS" : "PERSONALAUSWEIS",
                        caseIndex % 9 == 0,
                        caseIndex % 9 == 0 ? "Bevollmaechtigte Person " + caseIndex : null,
                        caseIndex % 9 == 0 ? "Nebenstrasse " + caseIndex : null,
                        caseIndex % 9 == 0 ? tenant.postalCode() : null,
                        caseIndex % 9 == 0 ? tenant.city() : null,
                        caseIndex % 9 == 0 ? "data:application/pdf;base64,UE9BX0RFTU8=" : null
                ),
                positions,
                List.of(
                        new PawnTicket(
                                "loan-ticket-" + tenant.key() + "-" + String.format("%04d", caseIndex),
                                "VT-" + tenant.numberPrefix() + "-" + String.format("%05d", caseIndex),
                                "VTBC-" + tenant.numberPrefix() + "-" + String.format("%05d", caseIndex),
                                "PS-" + tenant.numberPrefix() + "-" + String.format("%05d", caseIndex),
                                "2026-03",
                                "Allgemeine Pfandbedingungen fuer Demo-Daten",
                                recordedAt.plusSeconds(1_800),
                                LocalDate.now().plusMonths(4).plusDays(caseIndex % 14),
                                LocalDate.now().plusMonths(6).plusDays(caseIndex % 14),
                                4,
                                totalLoanValue,
                                monthlyInterestRate,
                                monthlyOperatingFee,
                                caseIndex % 7 == 0,
                                totalInterestAmount,
                                totalOperatingFeeAmount,
                                totalRepaymentAmount,
                                "Pfandrecht gemaess Demo-Setup.",
                                positions.stream().map(position -> new PawnTicketPosition(
                                        "IT-" + tenant.numberPrefix() + "-" + position.ticketGroup(),
                                        "BC-" + tenant.numberPrefix() + "-" + position.ticketGroup(),
                                        position.label(),
                                        position.description(),
                                        position.pledgedValue()
                                )).toList()
                        )
                )
        );
    }

    private int tenantCount(String scale) {
        return switch (normalize(scale)) {
            case "small" -> 2;
            case "large" -> ReferenceDataSeeder.TENANTS.size();
            default -> 4;
        };
    }

    private int casesPerTenant(String scale) {
        return switch (normalize(scale)) {
            case "small" -> 8;
            case "large" -> 42;
            default -> 18;
        };
    }

    private String normalize(String scale) {
        return scale == null ? "medium" : scale.trim().toLowerCase();
    }
}
