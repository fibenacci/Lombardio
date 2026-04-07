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
import io.lombardio.platform.tenant.application.BranchView;
import io.lombardio.platform.tenant.application.CreateTenantBranchCommand;
import io.lombardio.platform.tenant.application.CreateTenantUserCommand;
import io.lombardio.platform.tenant.application.TenantCatalogService;
import io.lombardio.platform.tenant.application.TenantFeatureView;
import io.lombardio.platform.tenant.application.TenantUserView;
import io.lombardio.platform.tenant.application.UpdateTenantUserCommand;
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
    return tenantCatalogService.listTenantUsers(tenantId).stream()
        .map(this::toTenantUserResponse)
        .toList();
  }

  @PostMapping("/users")
  public TenantUserResponse createUser(
      @PathVariable String tenantId,
      @Valid @RequestBody CreateTenantUserRequest request,
      @AuthenticationPrincipal AuthenticatedUser user) {
    authorizationService.requireTenantUserWrite(user, tenantId);
    return toTenantUserResponse(
        tenantCatalogService.createTenantUser(
            tenantId,
            new CreateTenantUserCommand(
                request.email(),
                request.password(),
                request.displayName(),
                request.roles(),
                request.branchIds())));
  }

  @PatchMapping("/users/{userId}")
  public TenantUserResponse updateUser(
      @PathVariable String tenantId,
      @PathVariable String userId,
      @Valid @RequestBody UpdateTenantUserRequest request,
      @AuthenticationPrincipal AuthenticatedUser user) {
    authorizationService.requireTenantUserWrite(user, tenantId);
    return toTenantUserResponse(
        tenantCatalogService.updateTenantUser(
            tenantId,
            userId,
            new UpdateTenantUserCommand(
                request.email(),
                request.displayName(),
                request.status(),
                request.roles(),
                request.branchIds())));
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
    return tenantCatalogService.listFeatures(tenantId).stream()
        .map(this::toFeatureResponse)
        .toList();
  }

  @GetMapping("/branches")
  public List<BranchResponse> listBranches(
      @PathVariable String tenantId, @AuthenticationPrincipal AuthenticatedUser user) {
    authorizationService.requireTenantBranchRead(user, tenantId);
    return tenantCatalogService.listBranches(tenantId).stream()
        .map(this::toBranchResponse)
        .toList();
  }

  @PostMapping("/branches")
  public BranchResponse createBranch(
      @PathVariable String tenantId,
      @Valid @RequestBody CreateTenantBranchRequest request,
      @AuthenticationPrincipal AuthenticatedUser user) {
    authorizationService.requireTenantBranchWrite(user, tenantId);
    return toBranchResponse(
        tenantCatalogService.createBranch(
            tenantId,
            new CreateTenantBranchCommand(request.key(), request.displayName(), request.status())));
  }

  private TenantUserResponse toTenantUserResponse(TenantUserView user) {
    return new TenantUserResponse(
        user.id(),
        user.username(),
        user.email(),
        user.displayName(),
        user.status(),
        user.roleIds(),
        user.branchIds());
  }

  private TenantFeatureResponse toFeatureResponse(TenantFeatureView feature) {
    return new TenantFeatureResponse(
        feature.tenantId(), feature.featureKey(), feature.enabled(), feature.updatedAt());
  }

  private BranchResponse toBranchResponse(BranchView branch) {
    return new BranchResponse(branch.id(), branch.key(), branch.displayName(), branch.status());
  }
}
