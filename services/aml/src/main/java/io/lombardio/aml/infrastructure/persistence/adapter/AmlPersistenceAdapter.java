package io.lombardio.aml.infrastructure.persistence.adapter;

import io.lombardio.aml.domain.model.AmlCase;
import io.lombardio.aml.domain.port.AmlRepository;
import io.lombardio.aml.infrastructure.persistence.entity.AmlCaseEntity;
import io.lombardio.aml.infrastructure.persistence.repository.SpringDataAmlRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AmlPersistenceAdapter implements AmlRepository {

    private final SpringDataAmlRepository repository;

    public AmlPersistenceAdapter(SpringDataAmlRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<AmlCase> findByTenantIdAndCustomerId(String tenantId, String customerId) {
        return repository.findByTenantIdAndCustomerId(tenantId, customerId).map(this::toDomain);
    }

    @Override
    public AmlCase save(AmlCase amlCase) {
        return toDomain(repository.save(toEntity(amlCase)));
    }

    private AmlCaseEntity toEntity(AmlCase amlCase) {
        AmlCaseEntity entity = new AmlCaseEntity();
        entity.setId(amlCase.id());
        entity.setTenantId(amlCase.tenantId());
        entity.setCustomerId(amlCase.customerId());
        entity.setStatus(amlCase.status());
        entity.setRiskLevel(amlCase.riskLevel());
        entity.setPepFlag(amlCase.pepFlag());
        entity.setSanctionsHit(amlCase.sanctionsHit());
        entity.setUnusualTransactionFlag(amlCase.unusualTransactionFlag());
        entity.setSourceOfFundsChecked(amlCase.sourceOfFundsChecked());
        entity.setSuspiciousActivityReported(amlCase.suspiciousActivityReported());
        entity.setGoamlReference(amlCase.goamlReference());
        entity.setDecisionNote(amlCase.decisionNote());
        entity.setLastScreenedAt(amlCase.lastScreenedAt());
        entity.setReviewedAt(amlCase.reviewedAt());
        entity.setUpdatedAt(amlCase.updatedAt());
        return entity;
    }

    private AmlCase toDomain(AmlCaseEntity entity) {
        return new AmlCase(
                entity.getId(),
                entity.getTenantId(),
                entity.getCustomerId(),
                entity.getStatus(),
                entity.getRiskLevel(),
                entity.isPepFlag(),
                entity.isSanctionsHit(),
                entity.isUnusualTransactionFlag(),
                entity.isSourceOfFundsChecked(),
                entity.isSuspiciousActivityReported(),
                entity.getGoamlReference(),
                entity.getDecisionNote(),
                entity.getLastScreenedAt(),
                entity.getReviewedAt(),
                entity.getUpdatedAt()
        );
    }
}
