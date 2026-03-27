package io.lombardio.identity.aml.domain.port;

import io.lombardio.identity.aml.domain.model.AmlCase;

import java.util.Optional;

public interface AmlRepository {

    Optional<AmlCase> findByTenantIdAndCustomerId(String tenantId, String customerId);

    AmlCase save(AmlCase amlCase);
}
