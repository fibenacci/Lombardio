package io.lombardio.aml.infrastructure.persistence.support;

import io.lombardio.aml.domain.model.AmlCase;
import io.lombardio.aml.domain.port.AmlRepository;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryAmlRepository implements AmlRepository {

    private final Map<String, AmlCase> store = new LinkedHashMap<>();

    @Override
    public Optional<AmlCase> findByTenantIdAndCustomerId(String tenantId, String customerId) {
        return store.values().stream()
                .filter(item -> item.tenantId().equals(tenantId) && item.customerId().equals(customerId))
                .findFirst();
    }

    @Override
    public AmlCase save(AmlCase amlCase) {
        store.put(amlCase.id(), amlCase);
        return amlCase;
    }
}
