package io.lombardio.loanorigination.domain.port;

import io.lombardio.loanorigination.domain.model.ValuationGuideline;

import java.util.List;
import java.util.Optional;

public interface ValuationGuidelineRepository {

    List<ValuationGuideline> findByTenantId(String tenantId);

    Optional<ValuationGuideline> findById(String id);
}
