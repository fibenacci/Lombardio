package io.lombardio.kyc.demo;

import io.lombardio.kyc.domain.KycRecord;
import io.lombardio.kyc.domain.KycRepository;
import io.lombardio.kyc.domain.KycStatus;
import io.lombardio.kyc.domain.KycVerificationMode;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
class ScenarioDataSeeder {

    private record DemoTenant(String id, String key) {
    }

    private static final List<DemoTenant> TENANTS = List.of(
            new DemoTenant("tenant-default", "default"),
            new DemoTenant("tenant-hamburg", "hanseatic"),
            new DemoTenant("tenant-munich", "isar"),
            new DemoTenant("tenant-cologne", "rhein"),
            new DemoTenant("tenant-stuttgart", "neckar")
    );

    private final KycRepository kycRepository;
    private final DemoDataProperties demoDataProperties;

    ScenarioDataSeeder(KycRepository kycRepository, DemoDataProperties demoDataProperties) {
        this.kycRepository = kycRepository;
        this.demoDataProperties = demoDataProperties;
    }

    void seed() {
        int tenantCount = tenantCount(demoDataProperties.effectiveScale());
        int customersPerTenant = customersPerTenant(demoDataProperties.effectiveScale());

        for (int tenantIndex = 0; tenantIndex < tenantCount; tenantIndex++) {
            DemoTenant tenant = TENANTS.get(tenantIndex);
            for (int customerIndex = 1; customerIndex <= customersPerTenant; customerIndex++) {
                kycRepository.save(buildRecord(tenant, tenantIndex, customerIndex));
            }
        }
    }

    private KycRecord buildRecord(DemoTenant tenant, int tenantIndex, int customerIndex) {
        KycStatus status = switch (customerIndex % 8) {
            case 0, 4, 5 -> KycStatus.APPROVED;
            case 1, 6 -> KycStatus.IN_PROGRESS;
            case 2 -> KycStatus.REJECTED;
            default -> KycStatus.NOT_STARTED;
        };
        KycVerificationMode mode = customerIndex % 3 == 0 ? KycVerificationMode.PROVIDER : KycVerificationMode.MANUAL;
        LocalDate today = LocalDate.now();

        return new KycRecord(
                "kyc-" + tenant.key() + "-" + String.format("%04d", customerIndex),
                tenant.id(),
                "customer-" + tenant.key() + "-" + String.format("%04d", customerIndex),
                mode,
                status,
                status == KycStatus.APPROVED ? today.plusYears(1).plusDays(customerIndex % 180) : null,
                customerIndex % 5 == 0 ? "REISEPASS" : "PERSONALAUSWEIS",
                tenant.key().substring(0, Math.min(3, tenant.key().length())).toUpperCase() + String.format("%05d", customerIndex),
                today.plusYears(2).plusDays(customerIndex % 365),
                "data:image/png;base64,ZXhhbXBsZS1mcm9udA==",
                "data:image/png;base64,ZXhhbXBsZS1iYWNr",
                decisionNote(status, mode, customerIndex),
                mode == KycVerificationMode.PROVIDER ? "DemoIdent" : null,
                mode == KycVerificationMode.PROVIDER ? "DEMO-" + tenant.key() + "-" + String.format("%05d", customerIndex) : null,
                mode == KycVerificationMode.PROVIDER ? status.name() : null
        );
    }

    private String decisionNote(KycStatus status, KycVerificationMode mode, int customerIndex) {
        return switch (status) {
            case APPROVED -> mode == KycVerificationMode.PROVIDER ? "Automatisierte Identifizierung erfolgreich abgeschlossen" : "Manuelle Identitaetspruefung freigegeben";
            case IN_PROGRESS -> "Dokumentenpruefung und Adressabgleich noch offen";
            case REJECTED -> customerIndex % 2 == 0 ? "Dokument nicht mehr gueltig" : "Abweichung bei Personenangaben";
            case NOT_STARTED -> "KYC-Pruefung noch nicht initiiert";
        };
    }

    private int tenantCount(String scale) {
        return switch (normalize(scale)) {
            case "small" -> 2;
            case "large" -> TENANTS.size();
            default -> 4;
        };
    }

    private int customersPerTenant(String scale) {
        return switch (normalize(scale)) {
            case "small" -> 12;
            case "large" -> 90;
            default -> 36;
        };
    }

    private String normalize(String scale) {
        return scale == null ? "medium" : scale.trim().toLowerCase();
    }
}
