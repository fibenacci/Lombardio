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
import io.lombardio.platform.tenant.application.CreateTenantCommand;
import io.lombardio.platform.tenant.application.CreateTenantUserCommand;
import io.lombardio.platform.tenant.application.TenantCatalogService;
import io.lombardio.platform.tenant.application.TenantFeatureView;
import io.lombardio.platform.tenant.application.TenantUserView;
import io.lombardio.platform.tenant.application.TenantView;
import io.lombardio.platform.tenant.application.UpdateTenantCommand;
import io.lombardio.platform.tenant.application.UpsertTenantFeatureCommand;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/platform/operator/tenants")
public class TenantController {

  private final TenantCatalogService tenantCatalogService;

  public TenantController(TenantCatalogService tenantCatalogService) {
    this.tenantCatalogService = tenantCatalogService;
  }

  @GetMapping
  @PreAuthorize("hasAuthority('platform.tenants.read')")
  public List<TenantResponse> listTenants(@AuthenticationPrincipal AuthenticatedUser user) {
    return tenantCatalogService.listTenants(user).stream().map(this::toTenantResponse).toList();
  }

  @PostMapping
  @PreAuthorize("hasAuthority('platform.tenants.write')")
  public TenantResponse createTenant(
      @Valid @RequestBody CreateTenantRequest request,
      @AuthenticationPrincipal AuthenticatedUser user) {
    return toTenantResponse(
        tenantCatalogService.createTenant(
            user, new CreateTenantCommand(request.key(), request.displayName(), request.status())));
  }

  @PatchMapping("/{id}")
  @PreAuthorize("hasAuthority('platform.tenants.write')")
  public TenantResponse updateTenant(
      @PathVariable String id,
      @Valid @RequestBody UpdateTenantRequest request,
      @AuthenticationPrincipal AuthenticatedUser user) {
    return toTenantResponse(
        tenantCatalogService.updateTenant(
            user,
            id,
            new UpdateTenantCommand(request.key(), request.displayName(), request.status())));
  }

  @GetMapping("/{id}/features")
  @PreAuthorize("hasAuthority('platform.tenants.read')")
  public List<TenantFeatureResponse> listFeatures(
      @PathVariable String id, @AuthenticationPrincipal AuthenticatedUser user) {
    return tenantCatalogService.listFeatures(user, id).stream()
        .map(this::toFeatureResponse)
        .toList();
  }

  @PutMapping("/{id}/features/{featureKey}")
  @PreAuthorize("hasAuthority('platform.tenants.write')")
  public TenantFeatureResponse upsertFeature(
      @PathVariable String id,
      @PathVariable String featureKey,
      @Valid @RequestBody UpsertTenantFeatureRequest request,
      @AuthenticationPrincipal AuthenticatedUser user) {
    return toFeatureResponse(
        tenantCatalogService.upsertFeature(
            user, id, featureKey, new UpsertTenantFeatureCommand(request.enabled())));
  }

  @PostMapping("/{id}/users")
  @PreAuthorize("hasAuthority('platform.tenants.write')")
  public TenantUserResponse createTenantUser(
      @PathVariable String id,
      @Valid @RequestBody CreateTenantUserRequest request,
      @AuthenticationPrincipal AuthenticatedUser user) {
    return toTenantUserResponse(
        tenantCatalogService.createTenantUser(
            user,
            id,
            new CreateTenantUserCommand(
                request.email(),
                request.password(),
                request.displayName(),
                request.roles(),
                request.branchIds())));
  }

  @GetMapping("/roles")
  @PreAuthorize("hasAuthority('platform.tenants.read')")
  public List<String> listAvailableRoles() {
    return tenantCatalogService.listAvailableRoles();
  }

  private TenantResponse toTenantResponse(TenantView tenant) {
    return new TenantResponse(
        tenant.id(),
        tenant.key(),
        tenant.displayName(),
        tenant.status(),
        tenant.createdAt(),
        tenant.updatedAt());
  }

  private TenantFeatureResponse toFeatureResponse(TenantFeatureView feature) {
    return new TenantFeatureResponse(
        feature.tenantId(), feature.featureKey(), feature.enabled(), feature.updatedAt());
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
}
