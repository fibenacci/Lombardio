/*
 * Lombardio Source-Available No-Distribution License 1.0
 *
 * Copyright (c) 2026 Benjamin Letzel. All rights reserved.
 *
 * This project is source-available for educational and review purposes only.
 * Redistribution, sublicensing, or commercial use is strictly prohibited.
 *
 * For partnership or cooperation inquiries, please contact the author.
 */
package io.lombardio.platform.infrastructure.persistence;

import io.lombardio.platform.tenant.domain.Branch;
import io.lombardio.platform.tenant.domain.BranchRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class BranchPersistenceAdapter implements BranchRepository {

  private final SpringDataBranchRepository repository;

  public BranchPersistenceAdapter(SpringDataBranchRepository repository) {
    this.repository = repository;
  }

  @Override
  public List<Branch> findByTenantId(String tenantId) {
    return repository.findByTenantIdOrderByCreatedAtAsc(tenantId).stream().map(this::toDomain).toList();
  }

  @Override
  public Optional<Branch> findByTenantIdAndKey(String tenantId, String key) {
    return repository.findByTenantIdAndKey(tenantId, key).map(this::toDomain);
  }

  @Override
  public Branch save(Branch branch) {
    return toDomain(repository.save(toEntity(branch)));
  }

  private BranchEntity toEntity(Branch branch) {
    BranchEntity entity = new BranchEntity();
    entity.setId(branch.id());
    entity.setTenantId(branch.tenantId());
    entity.setKey(branch.key());
    entity.setDisplayName(branch.displayName());
    entity.setStatus(branch.status());
    entity.setCreatedAt(branch.createdAt());
    entity.setUpdatedAt(branch.updatedAt());
    return entity;
  }

  private Branch toDomain(BranchEntity entity) {
    return new Branch(
        entity.getId(),
        entity.getTenantId(),
        entity.getKey(),
        entity.getDisplayName(),
        entity.getStatus(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}
