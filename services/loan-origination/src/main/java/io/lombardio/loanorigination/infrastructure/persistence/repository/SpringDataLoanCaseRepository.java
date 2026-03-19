package io.lombardio.loanorigination.infrastructure.persistence.repository;

import io.lombardio.loanorigination.infrastructure.persistence.entity.LoanCaseEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataLoanCaseRepository extends JpaRepository<LoanCaseEntity, String> {

    List<LoanCaseEntity> findByTenantId(String tenantId);

    List<LoanCaseEntity> findByTenantIdAndCustomerId(String tenantId, String customerId);
}
