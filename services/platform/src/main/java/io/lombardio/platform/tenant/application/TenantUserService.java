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

import io.lombardio.platform.iam.application.IdentityAdministration;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TenantUserService {

  private static final List<String> TENANT_ASSIGNABLE_ROLE_PREFIXES =
      List.of(
          "users.",
          "roles.",
          "branches.",
          "customers.",
          "kyc.",
          "aml.",
          "loans.",
          "auctions.",
          "pawn-tickets.",
          "cash-transactions.",
          "reporting.");

  private final IdentityAdministration identityAdministration;
  private final TenantLifecycleService tenantLifecycleService;
  private final TenantBranchService tenantBranchService;

  public TenantUserService(
      IdentityAdministration identityAdministration,
      TenantLifecycleService tenantLifecycleService,
      TenantBranchService tenantBranchService) {
    this.identityAdministration = identityAdministration;
    this.tenantLifecycleService = tenantLifecycleService;
    this.tenantBranchService = tenantBranchService;
  }

  public TenantUserView createTenantUser(String tenantId, CreateTenantUserCommand request) {
    tenantLifecycleService.requireTenant(tenantId);
    return identityAdministration.createTenantUser(
        tenantId,
        request.email(),
        request.password(),
        request.displayName(),
        request.roles(),
        tenantBranchService.sanitizeBranchIds(tenantId, request.branchIds()));
  }

  public List<String> listAvailableRoles() {
    return identityAdministration.getAvailableRoles();
  }

  public List<TenantUserView> listTenantUsers(String tenantId) {
    tenantLifecycleService.requireTenant(tenantId);
    return identityAdministration.listTenantUsers(tenantId);
  }

  public TenantUserView updateTenantUser(
      String tenantId, String userId, UpdateTenantUserCommand request) {
    tenantLifecycleService.requireTenant(tenantId);
    return identityAdministration.updateTenantUser(
        tenantId,
        userId,
        request.email(),
        request.displayName(),
        request.status(),
        request.roles(),
        tenantBranchService.sanitizeBranchIds(tenantId, request.branchIds()));
  }

  public List<String> listAvailableRolesForTenant(String tenantId) {
    tenantLifecycleService.requireTenant(tenantId);
    return identityAdministration.getAvailableRoles().stream()
        .filter(this::isTenantAssignableRole)
        .toList();
  }

  private boolean isTenantAssignableRole(String roleName) {
    return TENANT_ASSIGNABLE_ROLE_PREFIXES.stream().anyMatch(roleName::startsWith);
  }
}
