package io.lombardio.platform.tenant.api;

import io.lombardio.platform.security.AuthenticatedPlatformUser;
import io.lombardio.platform.security.PlatformAuthorizationService;
import io.lombardio.platform.tenant.application.TenantCatalogService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/platform/tenants")
public class TenantController {

    private final TenantCatalogService tenantCatalogService;
    private final PlatformAuthorizationService platformAuthorizationService;

    public TenantController(
            TenantCatalogService tenantCatalogService,
            PlatformAuthorizationService platformAuthorizationService
    ) {
        this.tenantCatalogService = tenantCatalogService;
        this.platformAuthorizationService = platformAuthorizationService;
    }

    @GetMapping
    public List<TenantResponse> listTenants(@AuthenticationPrincipal AuthenticatedPlatformUser principal) {
        platformAuthorizationService.requirePlatformRead(principal);
        return tenantCatalogService.listTenants();
    }

    @PostMapping
    public TenantResponse createTenant(
            @AuthenticationPrincipal AuthenticatedPlatformUser principal,
            @Valid @RequestBody CreateTenantRequest request
    ) {
        platformAuthorizationService.requirePlatformWrite(principal);
        return tenantCatalogService.createTenant(request);
    }

    @PatchMapping("/{id}")
    public TenantResponse updateTenant(
            @AuthenticationPrincipal AuthenticatedPlatformUser principal,
            @PathVariable String id,
            @Valid @RequestBody UpdateTenantRequest request
    ) {
        platformAuthorizationService.requirePlatformWrite(principal);
        return tenantCatalogService.updateTenant(id, request);
    }

    @GetMapping("/{id}/features")
    public List<TenantFeatureResponse> listFeatures(
            @AuthenticationPrincipal AuthenticatedPlatformUser principal,
            @PathVariable String id
    ) {
        platformAuthorizationService.requireTenantFeatureRead(principal, id);
        return tenantCatalogService.listFeatures(id);
    }

    @PutMapping("/{id}/features/{featureKey}")
    public TenantFeatureResponse upsertFeature(
            @AuthenticationPrincipal AuthenticatedPlatformUser principal,
            @PathVariable String id,
            @PathVariable String featureKey,
            @Valid @RequestBody UpsertTenantFeatureRequest request
    ) {
        platformAuthorizationService.requirePlatformWrite(principal);
        return tenantCatalogService.upsertFeature(id, featureKey, request);
    }
}
