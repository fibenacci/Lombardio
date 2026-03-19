package io.lombardio.kyc.infrastructure.persistence;

import io.lombardio.kyc.domain.KycRecord;
import io.lombardio.kyc.domain.KycRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class KycPersistenceAdapter implements KycRepository {

    private final SpringDataKycRepository repository;

    public KycPersistenceAdapter(SpringDataKycRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<KycRecord> findByTenantIdAndCustomerId(String tenantId, String customerId) {
        return repository.findByTenantIdAndCustomerId(tenantId, customerId).map(this::toDomain);
    }

    @Override
    public KycRecord save(KycRecord kycRecord) {
        return toDomain(repository.save(toEntity(kycRecord)));
    }

    private KycRecordEntity toEntity(KycRecord record) {
        KycRecordEntity entity = new KycRecordEntity();
        entity.setId(record.id());
        entity.setTenantId(record.tenantId());
        entity.setCustomerId(record.customerId());
        entity.setVerificationMode(record.verificationMode());
        entity.setStatus(record.status());
        entity.setVerifiedUntil(record.verifiedUntil());
        entity.setDocumentType(record.documentType());
        entity.setDocumentNumber(record.documentNumber());
        entity.setDocumentValidUntil(record.documentValidUntil());
        entity.setDocumentFrontImageDataUrl(record.documentFrontImageDataUrl());
        entity.setDocumentBackImageDataUrl(record.documentBackImageDataUrl());
        entity.setDecisionNote(record.decisionNote());
        entity.setProviderName(record.providerName());
        entity.setProviderReference(record.providerReference());
        entity.setProviderStatus(record.providerStatus());
        return entity;
    }

    private KycRecord toDomain(KycRecordEntity entity) {
        return new KycRecord(
                entity.getId(),
                entity.getTenantId(),
                entity.getCustomerId(),
                entity.getVerificationMode(),
                entity.getStatus(),
                entity.getVerifiedUntil(),
                entity.getDocumentType(),
                entity.getDocumentNumber(),
                entity.getDocumentValidUntil(),
                entity.getDocumentFrontImageDataUrl(),
                entity.getDocumentBackImageDataUrl(),
                entity.getDecisionNote(),
                entity.getProviderName(),
                entity.getProviderReference(),
                entity.getProviderStatus()
        );
    }
}
