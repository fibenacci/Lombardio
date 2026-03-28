/*
 * Lombardio Source-Available No-Distribution License 1.0
 *
 * Copyright (c) 2026 Benjamin Letzel. All rights reserved.
 *
 * This project is source-available for educational and review purposes only.
 * Redistribution, sublicensing, or commercial use is strictly prohibited.
 *
 * For partnership or cooperation inquiries, please contact the author.
 */
package io.lombardio.platform.tenant.domain;

import java.util.List;
import java.util.Optional;

public interface TenantRepository {

  List<Tenant> findAll();

  Optional<Tenant> findById(String id);

  Optional<Tenant> findByKey(String key);

  Tenant save(Tenant tenant);
}
