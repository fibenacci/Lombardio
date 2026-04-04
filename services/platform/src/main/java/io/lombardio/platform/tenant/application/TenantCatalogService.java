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
import io.lombardio.platform.tenant.api.CreateTenantRequest;
import io.lombardio.platform.tenant.api.CreateTenantUserRequest;
import io.lombardio.platform.tenant.api.TenantFeatureResponse;
import io.lombardio.platform.tenant.api.TenantResponse;
import io.lombardio.platform.tenant.api.TenantUserResponse;
import io.lombardio.platform.tenant.api.UpdateTenantRequest;
import io.lombardio.platform.tenant.api.UpdateTenantUserRequest;
import io.lombardio.platform.tenant.api.UpsertTenantFeatureRequest;
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

  public List<TenantResponse> listTenants() {
    return tenantLifecycleService.listTenants();
  }

  public TenantResponse createTenant(CreateTenantRequest request) {
    return tenantLifecycleService.createTenant(request);
  }

  public TenantUserResponse createTenantUser(String tenantId, CreateTenantUserRequest request) {
    return tenantUserService.createTenantUser(tenantId, request);
  }

  public List<String> listAvailableRoles() {
    return tenantUserService.listAvailableRoles();
  }

  public List<TenantUserResponse> listTenantUsers(String tenantId) {
    return tenantUserService.listTenantUsers(tenantId);
  }

  public TenantUserResponse updateTenantUser(
      String tenantId, String userId, UpdateTenantUserRequest request) {
    return tenantUserService.updateTenantUser(tenantId, userId, request);
  }

  public List<String> listAvailableRolesForTenant(String tenantId) {
    return tenantUserService.listAvailableRolesForTenant(tenantId);
  }

  public List<BranchResponse> listBranches(String tenantId) {
    return tenantBranchService.listBranches(tenantId);
  }

  public BranchResponse createBranch(String tenantId, CreateTenantBranchRequest request) {
    return tenantBranchService.createBranch(tenantId, request);
  }

  public TenantResponse updateTenant(String tenantId, UpdateTenantRequest request) {
    return tenantLifecycleService.updateTenant(tenantId, request);
  }

  public List<TenantFeatureResponse> listFeatures(String tenantId) {
    return tenantFeatureService.listFeatures(tenantId);
  }

  public TenantFeatureResponse upsertFeature(
      String tenantId, String featureKey, UpsertTenantFeatureRequest request) {
    return tenantFeatureService.upsertFeature(tenantId, featureKey, request);
  }
}
