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
package io.lombardio.platform.tenant.api;

import io.lombardio.platform.security.AuthenticatedUser;
import io.lombardio.platform.tenant.application.TenantCatalogService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/platform/operator/tenants/{tenantId}")
public class TenantAdministrationController {

  private final TenantCatalogService tenantCatalogService;
  private final TenantAdministrationAuthorizationService authorizationService;

  public TenantAdministrationController(
      TenantCatalogService tenantCatalogService,
      TenantAdministrationAuthorizationService authorizationService) {
    this.tenantCatalogService = tenantCatalogService;
    this.authorizationService = authorizationService;
  }

  @GetMapping("/users")
  public List<TenantUserResponse> listUsers(
      @PathVariable String tenantId, @AuthenticationPrincipal AuthenticatedUser user) {
    authorizationService.requireTenantUserRead(user, tenantId);
    return tenantCatalogService.listTenantUsers(tenantId);
  }

  @PostMapping("/users")
  public TenantUserResponse createUser(
      @PathVariable String tenantId,
      @Valid @RequestBody CreateTenantUserRequest request,
      @AuthenticationPrincipal AuthenticatedUser user) {
    authorizationService.requireTenantUserWrite(user, tenantId);
    return tenantCatalogService.createTenantUser(tenantId, request);
  }

  @PatchMapping("/users/{userId}")
  public TenantUserResponse updateUser(
      @PathVariable String tenantId,
      @PathVariable String userId,
      @Valid @RequestBody UpdateTenantUserRequest request,
      @AuthenticationPrincipal AuthenticatedUser user) {
    authorizationService.requireTenantUserWrite(user, tenantId);
    return tenantCatalogService.updateTenantUser(tenantId, userId, request);
  }

  @GetMapping("/roles")
  public List<String> listRoles(
      @PathVariable String tenantId, @AuthenticationPrincipal AuthenticatedUser user) {
    authorizationService.requireTenantRoleRead(user, tenantId);
    return tenantCatalogService.listAvailableRolesForTenant(tenantId);
  }

  @GetMapping("/features")
  public List<TenantFeatureResponse> listFeatures(
      @PathVariable String tenantId, @AuthenticationPrincipal AuthenticatedUser user) {
    authorizationService.requireTenantFeatureRead(user, tenantId);
    return tenantCatalogService.listFeatures(tenantId);
  }

  @GetMapping("/branches")
  public List<BranchResponse> listBranches(
      @PathVariable String tenantId, @AuthenticationPrincipal AuthenticatedUser user) {
    authorizationService.requireTenantBranchRead(user, tenantId);
    return tenantCatalogService.listBranches(tenantId);
  }

  @PostMapping("/branches")
  public BranchResponse createBranch(
      @PathVariable String tenantId,
      @Valid @RequestBody CreateTenantBranchRequest request,
      @AuthenticationPrincipal AuthenticatedUser user) {
    authorizationService.requireTenantBranchWrite(user, tenantId);
    return tenantCatalogService.createBranch(tenantId, request);
  }
}
