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

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TenantCatalogService {

  private final TenantLifecycleService tenantLifecycleService;
  private final TenantFeatureService tenantFeatureService;
  private final TenantBranchService tenantBranchService;
  private final TenantUserService tenantUserService;

  public TenantCatalogService(
      TenantLifecycleService tenantLifecycleService,
      TenantFeatureService tenantFeatureService,
      TenantBranchService tenantBranchService,
      TenantUserService tenantUserService) {
    this.tenantLifecycleService = tenantLifecycleService;
    this.tenantFeatureService = tenantFeatureService;
    this.tenantBranchService = tenantBranchService;
    this.tenantUserService = tenantUserService;
  }

  public List<TenantView> listTenants() {
    return tenantLifecycleService.listTenants();
  }

  public TenantView createTenant(CreateTenantCommand request) {
    return tenantLifecycleService.createTenant(request);
  }

  public TenantUserView createTenantUser(String tenantId, CreateTenantUserCommand request) {
    return tenantUserService.createTenantUser(tenantId, request);
  }

  public List<String> listAvailableRoles() {
    return tenantUserService.listAvailableRoles();
  }

  public List<TenantUserView> listTenantUsers(String tenantId) {
    return tenantUserService.listTenantUsers(tenantId);
  }

  public TenantUserView updateTenantUser(
      String tenantId, String userId, UpdateTenantUserCommand request) {
    return tenantUserService.updateTenantUser(tenantId, userId, request);
  }

  public List<String> listAvailableRolesForTenant(String tenantId) {
    return tenantUserService.listAvailableRolesForTenant(tenantId);
  }

  public List<BranchView> listBranches(String tenantId) {
    return tenantBranchService.listBranches(tenantId);
  }

  public BranchView createBranch(String tenantId, CreateTenantBranchCommand request) {
    return tenantBranchService.createBranch(tenantId, request);
  }

  public TenantView updateTenant(String tenantId, UpdateTenantCommand request) {
    return tenantLifecycleService.updateTenant(tenantId, request);
  }

  public List<TenantFeatureView> listFeatures(String tenantId) {
    return tenantFeatureService.listFeatures(tenantId);
  }

  public TenantFeatureView upsertFeature(
      String tenantId, String featureKey, UpsertTenantFeatureCommand request) {
    return tenantFeatureService.upsertFeature(tenantId, featureKey, request);
  }
}
