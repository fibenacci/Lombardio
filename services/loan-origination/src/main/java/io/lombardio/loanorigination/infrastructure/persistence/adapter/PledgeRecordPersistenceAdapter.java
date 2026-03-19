package io.lombardio.loanorigination.infrastructure.persistence.adapter;

import io.lombardio.loanorigination.domain.model.PledgeRecord;
import io.lombardio.loanorigination.domain.port.PledgeRecordRepository;
import io.lombardio.loanorigination.infrastructure.persistence.entity.LoanCaseEntity;
import io.lombardio.loanorigination.infrastructure.persistence.entity.PledgeRecordEntity;
import io.lombardio.loanorigination.infrastructure.persistence.repository.SpringDataPledgeRecordRepository;
import org.springframework.stereotype.Repository;

@Repository
public class PledgeRecordPersistenceAdapter implements PledgeRecordRepository {

    private final SpringDataPledgeRecordRepository repository;

    public PledgeRecordPersistenceAdapter(SpringDataPledgeRecordRepository repository) {
        this.repository = repository;
    }

    @Override
    public PledgeRecord save(PledgeRecord pledgeRecord) {
        return toDomain(repository.save(toEntity(pledgeRecord)));
    }

    private PledgeRecordEntity toEntity(PledgeRecord pledgeRecord) {
        PledgeRecordEntity entity = new PledgeRecordEntity();
        entity.setId(pledgeRecord.id());
        LoanCaseEntity loanCase = new LoanCaseEntity();
        loanCase.setId(pledgeRecord.loanCaseId());
        entity.setLoanCase(loanCase);
        entity.setTenantId(pledgeRecord.tenantId());
        entity.setRecordedAt(pledgeRecord.recordedAt());
        entity.setLanguageCode(pledgeRecord.languageCode());
        entity.setRetentionUntil(pledgeRecord.retentionUntil());
        entity.setPledgorName(pledgeRecord.pledgorName());
        entity.setPledgorStreet(pledgeRecord.pledgorStreet());
        entity.setPledgorPostalCode(pledgeRecord.pledgorPostalCode());
        entity.setPledgorCity(pledgeRecord.pledgorCity());
        entity.setPledgorBirthDate(pledgeRecord.pledgorBirthDate());
        entity.setCheckedDocumentType(pledgeRecord.checkedDocumentType());
        entity.setPowerOfAttorneyRequired(pledgeRecord.powerOfAttorneyRequired());
        entity.setBearerName(pledgeRecord.bearerName());
        entity.setBearerStreet(pledgeRecord.bearerStreet());
        entity.setBearerPostalCode(pledgeRecord.bearerPostalCode());
        entity.setBearerCity(pledgeRecord.bearerCity());
        entity.setPowerOfAttorneyDocumentDataUrl(pledgeRecord.powerOfAttorneyDocumentDataUrl());
        return entity;
    }

    private PledgeRecord toDomain(PledgeRecordEntity entity) {
        return new PledgeRecord(
                entity.getId(),
                entity.getLoanCase().getId(),
                entity.getTenantId(),
                entity.getRecordedAt(),
                entity.getLanguageCode(),
                entity.getRetentionUntil(),
                entity.getPledgorName(),
                entity.getPledgorStreet(),
                entity.getPledgorPostalCode(),
                entity.getPledgorCity(),
                entity.getPledgorBirthDate(),
                entity.getCheckedDocumentType(),
                entity.isPowerOfAttorneyRequired(),
                entity.getBearerName(),
                entity.getBearerStreet(),
                entity.getBearerPostalCode(),
                entity.getBearerCity(),
                entity.getPowerOfAttorneyDocumentDataUrl()
        );
    }
}
