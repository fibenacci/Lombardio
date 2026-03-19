package io.lombardio.kyc.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataKycRepository extends JpaRepository<KycRecordEntity, String> {

    Optional<KycRecordEntity> findByTenantIdAndCustomerId(String tenantId, String customerId);
}
