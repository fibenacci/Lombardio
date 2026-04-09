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

import io.lombardio.platform.security.Audited;
import io.lombardio.platform.security.AuthenticatedUser;
import io.lombardio.platform.tenant.application.TenantAdministrationAuthorizationService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TenantCatalogService {

  private final TenantLifecycleService tenantLifecycleService;
  private final TenantFeatureService tenantFeatureService;
  private final TenantBranchService tenantBranchService;
  private final TenantUserService tenantUserService;
  private final TenantAdministrationAuthorizationService authorizationService;

  public TenantCatalogService(
      TenantLifecycleService tenantLifecycleService,
      TenantFeatureService tenantFeatureService,
      TenantBranchService tenantBranchService,
      TenantUserService tenantUserService,
      TenantAdministrationAuthorizationService authorizationService) {
    this.tenantLifecycleService = tenantLifecycleService;
    this.tenantFeatureService = tenantFeatureService;
    this.tenantBranchService = tenantBranchService;
    this.tenantUserService = tenantUserService;
    this.authorizationService = authorizationService;
  }

  public List<TenantView> listTenants(AuthenticatedUser user) {
    authorizationService.requirePermission(user, "platform.tenants.read");
    return tenantLifecycleService.listTenants();
  }

  @Audited(action = "CREATE_TENANT", targetType = "TENANT")
  public TenantView createTenant(AuthenticatedUser user, CreateTenantCommand request) {
    authorizationService.requirePermission(user, "platform.tenants.write");
    return tenantLifecycleService.createTenant(request);
  }

  @Audited(action = "CREATE_TENANT_USER", targetType = "TENANT_USER")
  public TenantUserView createTenantUser(
      AuthenticatedUser user, String tenantId, CreateTenantUserCommand request) {
    authorizationService.requireTenantUserWrite(user, tenantId);
    return tenantUserService.createTenantUser(tenantId, request);
  }

  public List<String> listAvailableRoles() {
    return tenantUserService.listAvailableRoles();
  }

  public List<TenantUserView> listTenantUsers(AuthenticatedUser user, String tenantId) {
    authorizationService.requireTenantUserRead(user, tenantId);
    return tenantUserService.listTenantUsers(tenantId);
  }

  @Audited(action = "UPDATE_TENANT_USER", targetType = "TENANT_USER")
  public TenantUserView updateTenantUser(
      AuthenticatedUser user, String tenantId, String userId, UpdateTenantUserCommand request) {
    authorizationService.requireTenantUserWrite(user, tenantId);
    return tenantUserService.updateTenantUser(tenantId, userId, request);
  }

  public List<String> listAvailableRolesForTenant(AuthenticatedUser user, String tenantId) {
    authorizationService.requireTenantRoleRead(user, tenantId);
    return tenantUserService.listAvailableRolesForTenant(tenantId);
  }

  public List<BranchView> listBranches(AuthenticatedUser user, String tenantId) {
    authorizationService.requireTenantBranchRead(user, tenantId);
    return tenantBranchService.listBranches(tenantId);
  }

  @Audited(action = "CREATE_BRANCH", targetType = "BRANCH")
  public BranchView createBranch(
      AuthenticatedUser user, String tenantId, CreateTenantBranchCommand request) {
    authorizationService.requireTenantBranchWrite(user, tenantId);
    return tenantBranchService.createBranch(tenantId, request);
  }

  @Audited(action = "UPDATE_TENANT", targetType = "TENANT")
  public TenantView updateTenant(
      AuthenticatedUser user, String tenantId, UpdateTenantCommand request) {
    authorizationService.requireTenantMatchOrPermission(user, tenantId, "platform.tenants.write");
    return tenantLifecycleService.updateTenant(tenantId, request);
  }

  public List<TenantFeatureView> listFeatures(AuthenticatedUser user, String tenantId) {
    authorizationService.requireTenantFeatureRead(user, tenantId);
    return tenantFeatureService.listFeatures(tenantId);
  }

  @Audited(action = "UPSERT_FEATURE", targetType = "FEATURE")
  public TenantFeatureView upsertFeature(
      AuthenticatedUser user,
      String tenantId,
      String featureKey,
      UpsertTenantFeatureCommand request) {
    authorizationService.requirePermission(user, "platform.tenants.write");
    return tenantFeatureService.upsertFeature(tenantId, featureKey, request);
  }
}
