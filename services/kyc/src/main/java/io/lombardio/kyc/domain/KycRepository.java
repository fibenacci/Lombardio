package io.lombardio.kyc.domain;

import java.util.Optional;

public interface KycRepository {

    Optional<KycRecord> findByTenantIdAndCustomerId(String tenantId, String customerId);

    KycRecord save(KycRecord kycRecord);
}
