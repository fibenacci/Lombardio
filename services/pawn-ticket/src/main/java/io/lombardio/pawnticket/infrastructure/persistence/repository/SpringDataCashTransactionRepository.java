package io.lombardio.pawnticket.infrastructure.persistence.repository;

import io.lombardio.pawnticket.infrastructure.persistence.entity.CashTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataCashTransactionRepository extends JpaRepository<CashTransactionEntity, String> {

    List<CashTransactionEntity> findByTenantIdOrderByCreatedAtDesc(String tenantId);
}
