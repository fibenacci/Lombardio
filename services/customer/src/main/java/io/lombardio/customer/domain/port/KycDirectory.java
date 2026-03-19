package io.lombardio.customer.domain.port;

public interface KycDirectory {

    KycProjection getStatus(String tenantId, String customerId);

    record KycProjection(
            String status,
            boolean approved,
            String documentType
    ) {
    }
}
