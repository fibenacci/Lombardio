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
package io.lombardio.platform.tenant.application;

import io.lombardio.platform.tenant.api.BranchResponse;
import io.lombardio.platform.tenant.api.CreateTenantBranchRequest;
import io.lombardio.platform.tenant.domain.Branch;
import io.lombardio.platform.tenant.domain.BranchRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantBranchService {

  private final BranchRepository branchRepository;
  private final TenantLifecycleService tenantLifecycleService;
  private final Clock clock;

  public TenantBranchService(
      BranchRepository branchRepository,
      TenantLifecycleService tenantLifecycleService,
      Clock clock) {
    this.branchRepository = branchRepository;
    this.tenantLifecycleService = tenantLifecycleService;
    this.clock = clock;
  }

  public List<BranchResponse> listBranches(String tenantId) {
    tenantLifecycleService.requireTenant(tenantId);
    return branchRepository.findByTenantId(tenantId).stream().map(this::toBranchResponse).toList();
  }

  @Transactional
  public BranchResponse createBranch(String tenantId, CreateTenantBranchRequest request) {
    tenantLifecycleService.requireTenant(tenantId);
    branchRepository
        .findByTenantIdAndKey(tenantId, request.key())
        .ifPresent(
            branch -> {
              throw new IllegalArgumentException("Branch key already exists: " + request.key());
            });

    Instant now = Instant.now(clock);
    Branch saved =
        branchRepository.save(
            new Branch(
                "branch-" + UUID.randomUUID(),
                tenantId,
                request.key(),
                request.displayName(),
                request.status() == null || request.status().isBlank() ? "ACTIVE" : request.status(),
                now,
                now));
    return toBranchResponse(saved);
  }

  protected List<String> sanitizeBranchIds(String tenantId, List<String> branchIds) {
    List<String> allowedBranchIds = branchRepository.findByTenantId(tenantId).stream().map(Branch::id).toList();
    List<String> requestedBranchIds = branchIds == null ? List.of() : branchIds;
    boolean invalidBranchPresent =
        requestedBranchIds.stream().anyMatch(branchId -> !allowedBranchIds.contains(branchId));
    if (invalidBranchPresent) {
      throw new IllegalArgumentException("One or more branch assignments are invalid for tenant " + tenantId);
    }
    return requestedBranchIds;
  }

  private BranchResponse toBranchResponse(Branch branch) {
    return new BranchResponse(branch.id(), branch.key(), branch.displayName(), branch.status());
  }
}
