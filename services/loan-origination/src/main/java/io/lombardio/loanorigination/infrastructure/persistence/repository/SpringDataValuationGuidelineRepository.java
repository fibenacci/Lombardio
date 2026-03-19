package io.lombardio.loanorigination.infrastructure.persistence.repository;

import io.lombardio.loanorigination.infrastructure.persistence.entity.ValuationGuidelineEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataValuationGuidelineRepository extends JpaRepository<ValuationGuidelineEntity, String> {

    List<ValuationGuidelineEntity> findByTenantIdOrderByCategoryAscLabelAsc(String tenantId);
}
