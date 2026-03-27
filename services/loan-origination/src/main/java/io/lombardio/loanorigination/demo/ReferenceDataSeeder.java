package io.lombardio.loanorigination.demo;

import io.lombardio.loanorigination.domain.model.ValuationGuideline;
import io.lombardio.loanorigination.infrastructure.persistence.adapter.ValuationGuidelinePersistenceAdapter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class ReferenceDataSeeder {

    public record DemoTenant(String id, String key, String numberPrefix, String city, String postalCode) {
    }

    public static final List<DemoTenant> TENANTS = List.of(
            new DemoTenant("tenant-default", "default", "BER", "Berlin", "10115"),
            new DemoTenant("tenant-hamburg", "hanseatic", "HAM", "Hamburg", "20095"),
            new DemoTenant("tenant-munich", "isar", "MUC", "Muenchen", "80331"),
            new DemoTenant("tenant-cologne", "rhein", "CGN", "Koeln", "50667"),
            new DemoTenant("tenant-stuttgart", "neckar", "STR", "Stuttgart", "70173")
    );

    private final ValuationGuidelinePersistenceAdapter guidelineRepository;
    private final DemoDataProperties demoDataProperties;

    ReferenceDataSeeder(ValuationGuidelinePersistenceAdapter guidelineRepository, DemoDataProperties demoDataProperties) {
        this.guidelineRepository = guidelineRepository;
        this.demoDataProperties = demoDataProperties;
    }

    void seed() {
        for (int tenantIndex = 0; tenantIndex < tenantCount(demoDataProperties.effectiveScale()); tenantIndex++) {
            seedGuidelinesForTenant(TENANTS.get(tenantIndex));
        }
    }

    private List<ValuationGuideline> seedGuidelinesForTenant(DemoTenant tenant) {
        List<ValuationGuideline> guidelines = List.of(
                new ValuationGuideline("guideline-" + tenant.key() + "-gold-585", tenant.id(), "Jewelry", "Gold 585", "Goldring 585", "Gelbgold 14 Karat", new BigDecimal("180.00")),
                new ValuationGuideline("guideline-" + tenant.key() + "-gold-750", tenant.id(), "Jewelry", "Gold 750", "Goldkette 750", "Gelbgold 18 Karat", new BigDecimal("320.00")),
                new ValuationGuideline("guideline-" + tenant.key() + "-silver-925", tenant.id(), "Jewelry", "Silver 925", "Silberarmband 925", "Sterlingsilber", new BigDecimal("45.00")),
                new ValuationGuideline("guideline-" + tenant.key() + "-watch", tenant.id(), "Luxury", "Watch", "Vintage Uhr", "Mechanische Armbanduhr", new BigDecimal("240.00")),
                new ValuationGuideline("guideline-" + tenant.key() + "-iphone", tenant.id(), "Electronics", "iPhone", "Apple iPhone 14 128GB", "gebraucht, funktionsfaehig", new BigDecimal("260.00")),
                new ValuationGuideline("guideline-" + tenant.key() + "-laptop", tenant.id(), "Electronics", "Laptop", "MacBook Air M1", "gebraucht, guter Zustand", new BigDecimal("420.00"))
        );

        List<ValuationGuideline> persisted = new ArrayList<>();
        for (ValuationGuideline guideline : guidelines) {
            persisted.add(guidelineRepository.save(guideline));
        }
        return persisted;
    }

    private int tenantCount(String scale) {
        return switch (normalize(scale)) {
            case "small" -> 2;
            case "large" -> TENANTS.size();
            default -> 4;
        };
    }

    private String normalize(String scale) {
        return scale == null ? "medium" : scale.trim().toLowerCase();
    }
}
