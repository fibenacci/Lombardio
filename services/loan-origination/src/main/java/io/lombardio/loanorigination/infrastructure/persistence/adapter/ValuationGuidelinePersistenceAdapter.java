package io.lombardio.loanorigination.infrastructure.persistence.adapter;

import io.lombardio.loanorigination.domain.model.ValuationGuideline;
import io.lombardio.loanorigination.domain.port.ValuationGuidelineRepository;
import io.lombardio.loanorigination.infrastructure.persistence.entity.ValuationGuidelineEntity;
import io.lombardio.loanorigination.infrastructure.persistence.repository.SpringDataValuationGuidelineRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ValuationGuidelinePersistenceAdapter implements ValuationGuidelineRepository {

    private final SpringDataValuationGuidelineRepository repository;

    public ValuationGuidelinePersistenceAdapter(SpringDataValuationGuidelineRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ValuationGuideline> findByTenantId(String tenantId) {
        return repository.findByTenantIdOrderByCategoryAscLabelAsc(tenantId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<ValuationGuideline> findById(String id) {
        return repository.findById(id).map(this::toDomain);
    }

    public ValuationGuideline save(ValuationGuideline guideline) {
        return toDomain(repository.save(toEntity(guideline)));
    }

    private ValuationGuidelineEntity toEntity(ValuationGuideline guideline) {
        ValuationGuidelineEntity entity = new ValuationGuidelineEntity();
        entity.setId(guideline.id());
        entity.setTenantId(guideline.tenantId());
        entity.setCategory(guideline.category());
        entity.setMaterial(guideline.material());
        entity.setLabel(guideline.label());
        entity.setDescription(guideline.description());
        entity.setBaseLoanValue(guideline.baseLoanValue());
        return entity;
    }

    private ValuationGuideline toDomain(ValuationGuidelineEntity entity) {
        return new ValuationGuideline(
                entity.getId(),
                entity.getTenantId(),
                entity.getCategory(),
                entity.getMaterial(),
                entity.getLabel(),
                entity.getDescription(),
                entity.getBaseLoanValue()
        );
    }
}
