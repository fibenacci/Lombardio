package io.lombardio.pawnticket.domain.port;

import io.lombardio.pawnticket.domain.model.CashTransaction;

import java.util.List;

public interface CashTransactionRepository {

    CashTransaction save(CashTransaction cashTransaction);

    List<CashTransaction> findByTenantId(String tenantId);
}
