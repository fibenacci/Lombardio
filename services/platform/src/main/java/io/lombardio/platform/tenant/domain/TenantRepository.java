package io.lombardio.platform.tenant.domain;

import java.util.List;
import java.util.Optional;

public interface TenantRepository {

    List<Tenant> findAll();

    Optional<Tenant> findById(String id);

    Optional<Tenant> findByKey(String key);

    Tenant save(Tenant tenant);
}
