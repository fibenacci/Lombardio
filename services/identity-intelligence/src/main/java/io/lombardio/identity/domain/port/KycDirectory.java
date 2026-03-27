package io.lombardio.identity.domain.port;

import java.util.Optional;

@FunctionalInterface
public interface KycDirectory {
    KycProjection getStatus(String tenantId, String customerId);

    default KycProjection getStatus(String tenantId, String customerId, Optional<String> accessToken) {
        return getStatus(tenantId, customerId);
    }

    record KycProjection(String status, boolean approved, String documentType) {
    }
}
