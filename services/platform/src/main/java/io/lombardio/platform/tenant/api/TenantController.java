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

import io.lombardio.platform.tenant.application.TenantCatalogService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
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
  public List<TenantResponse> listTenants() {
    return tenantCatalogService.listTenants();
  }

  @PostMapping
  @PreAuthorize("hasAuthority('platform.tenants.write')")
  public TenantResponse createTenant(@Valid @RequestBody CreateTenantRequest request) {
    return tenantCatalogService.createTenant(request);
  }

  @PatchMapping("/{id}")
  @PreAuthorize("hasAuthority('platform.tenants.write')")
  public TenantResponse updateTenant(
      @PathVariable String id, @Valid @RequestBody UpdateTenantRequest request) {
    return tenantCatalogService.updateTenant(id, request);
  }

  @GetMapping("/{id}/features")
  @PreAuthorize("hasAuthority('platform.tenants.read')")
  public List<TenantFeatureResponse> listFeatures(@PathVariable String id) {
    return tenantCatalogService.listFeatures(id);
  }

  @PutMapping("/{id}/features/{featureKey}")
  @PreAuthorize("hasAuthority('platform.tenants.write')")
  public TenantFeatureResponse upsertFeature(
      @PathVariable String id,
      @PathVariable String featureKey,
      @Valid @RequestBody UpsertTenantFeatureRequest request) {
    return tenantCatalogService.upsertFeature(id, featureKey, request);
  }

  @PostMapping("/{id}/users")
  @PreAuthorize("hasAuthority('platform.tenants.write')")
  public TenantUserResponse createTenantUser(
      @PathVariable String id, @Valid @RequestBody CreateTenantUserRequest request) {
    return tenantCatalogService.createTenantUser(id, request);
  }

  @GetMapping("/roles")
  @PreAuthorize("hasAuthority('platform.tenants.read')")
  public List<String> listAvailableRoles() {
    return tenantCatalogService.listAvailableRoles();
  }
}
