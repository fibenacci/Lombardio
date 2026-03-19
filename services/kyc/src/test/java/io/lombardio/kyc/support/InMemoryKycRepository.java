package io.lombardio.kyc.support;

import io.lombardio.kyc.domain.KycRecord;
import io.lombardio.kyc.domain.KycRepository;
import io.lombardio.kyc.domain.KycStatus;
import io.lombardio.kyc.domain.KycVerificationMode;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class InMemoryKycRepository implements KycRepository {

    private final Map<String, KycRecord> store = new LinkedHashMap<>();

    public InMemoryKycRepository() {
        save(new KycRecord(
                "kyc-1",
                "tenant-default",
                "customer-berlin-1",
                KycVerificationMode.MANUAL,
                KycStatus.APPROVED,
                LocalDate.now().plusYears(1),
                "PERSONALAUSWEIS",
                "L01X00T47",
                LocalDate.now().plusYears(4),
                "data:image/png;base64,ZXhhbXBsZS1mcm9udA==",
                "data:image/png;base64,ZXhhbXBsZS1iYWNr",
                "Identitaet geprueft",
                null,
                null,
                null
        ));
    }

    @Override
    public Optional<KycRecord> findByTenantIdAndCustomerId(String tenantId, String customerId) {
        return store.values().stream()
                .filter(record -> record.tenantId().equals(tenantId) && record.customerId().equals(customerId))
                .findFirst();
    }

    @Override
    public KycRecord save(KycRecord kycRecord) {
        store.put(kycRecord.id(), kycRecord);
        return kycRecord;
    }
}
