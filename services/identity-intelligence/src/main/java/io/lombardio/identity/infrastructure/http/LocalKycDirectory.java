package io.lombardio.identity.infrastructure.http;

import io.lombardio.identity.domain.port.KycDirectory;
import io.lombardio.identity.kyc.domain.KycRepository;
import io.lombardio.identity.kyc.domain.KycStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class LocalKycDirectory implements KycDirectory {

    private final KycRepository kycRepository;

    public LocalKycDirectory(KycRepository kycRepository) {
        this.kycRepository = kycRepository;
    }

    @Override
    public KycProjection getStatus(String tenantId, String customerId) {
        return getStatus(tenantId, customerId, Optional.empty());
    }

    @Override
    public KycProjection getStatus(String tenantId, String customerId, Optional<String> accessToken) {
        try {
            return kycRepository.findByTenantIdAndCustomerId(tenantId, customerId)
                    .map(record -> new KycProjection(
                            record.status().name(),
                            record.status() == KycStatus.APPROVED,
                            record.documentType()
                    ))
                    .orElse(new KycProjection("NOT_STARTED", false, null));
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to get local KYC status for customer " + customerId + ": " + e.getMessage());
            return new KycProjection("NOT_STARTED", false, null);
        }
    }
}
