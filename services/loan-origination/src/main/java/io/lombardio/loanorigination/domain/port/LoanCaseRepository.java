package io.lombardio.loanorigination.domain.port;

import io.lombardio.loanorigination.domain.model.LoanCase;

import java.util.List;

public interface LoanCaseRepository {

    LoanCase save(LoanCase loanCase);

    List<LoanCase> findByTenantId(String tenantId);

    List<LoanCase> findByTenantIdAndCustomerId(String tenantId, String customerId);
}
