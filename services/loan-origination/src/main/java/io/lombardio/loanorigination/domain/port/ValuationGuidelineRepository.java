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
package io.lombardio.loanorigination.domain.port;

import io.lombardio.loanorigination.domain.model.ValuationGuideline;
import java.util.List;
import java.util.Optional;

public interface ValuationGuidelineRepository {

  List<ValuationGuideline> findByTenantId(String tenantId);

  Optional<ValuationGuideline> findById(String id);
}
