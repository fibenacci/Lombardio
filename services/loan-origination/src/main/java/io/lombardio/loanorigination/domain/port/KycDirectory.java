package io.lombardio.loanorigination.domain.port;

import java.util.Optional;

@FunctionalInterface
public interface KycDirectory {
    KycProjection getStatus(String tenantId, String customerId);

    default KycProjection getStatus(String tenantId, String customerId, Optional<String> accessToken) {
        return getStatus(tenantId, customerId);
    }

    default boolean isApproved(String tenantId, String customerId) {
        return getStatus(tenantId, customerId).approved();
    }

    record KycProjection(String status, boolean approved, String documentType) {
    }
}
