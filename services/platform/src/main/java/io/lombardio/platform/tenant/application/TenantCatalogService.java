package io.lombardio.platform.tenant.application;

import io.lombardio.platform.tenant.api.CreateTenantRequest;
import io.lombardio.platform.tenant.api.TenantFeatureResponse;
import io.lombardio.platform.tenant.api.TenantResponse;
import io.lombardio.platform.tenant.api.UpdateTenantRequest;
import io.lombardio.platform.tenant.api.UpsertTenantFeatureRequest;
import io.lombardio.platform.tenant.domain.Tenant;
import io.lombardio.platform.tenant.domain.TenantFeature;
import io.lombardio.platform.tenant.domain.TenantFeatureRepository;
import io.lombardio.platform.tenant.domain.TenantRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class TenantCatalogService {

    public static final Set<String> SUPPORTED_FEATURE_KEYS = Set.of(
            "identity-access",
            "customer-management",
            "collateral-management",
            "aml-compliance",
            "kyc-provider-verification",
            "kyc-document-ocr",
            "auction-workflow",
            "online-auctions"
    );

    private final TenantRepository tenantRepository;
    private final TenantFeatureRepository tenantFeatureRepository;
    private final Clock clock;

    public TenantCatalogService(
            TenantRepository tenantRepository,
            TenantFeatureRepository tenantFeatureRepository,
            Clock clock
    ) {
        this.tenantRepository = tenantRepository;
        this.tenantFeatureRepository = tenantFeatureRepository;
        this.clock = clock;
    }

    public List<TenantResponse> listTenants() {
        return tenantRepository.findAll().stream().map(this::toTenantResponse).toList();
    }

    public TenantResponse createTenant(CreateTenantRequest request) {
        tenantRepository.findByKey(request.key()).ifPresent(tenant -> {
            throw new IllegalArgumentException("Tenant key already exists: " + request.key());
        });

        Instant now = Instant.now(clock);
        Tenant tenant = new Tenant(
                "tenant-" + UUID.randomUUID(),
                request.key(),
                request.displayName(),
                request.status(),
                now,
                now
        );

        return toTenantResponse(tenantRepository.save(tenant));
    }

    public TenantResponse updateTenant(String tenantId, UpdateTenantRequest request) {
        Tenant existing = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));

        Tenant updated = new Tenant(
                existing.id(),
                request.key(),
                request.displayName(),
                request.status(),
                existing.createdAt(),
                Instant.now(clock)
        );

        return toTenantResponse(tenantRepository.save(updated));
    }

    public List<TenantFeatureResponse> listFeatures(String tenantId) {
        requireTenant(tenantId);
        return tenantFeatureRepository.findByTenantId(tenantId).stream()
                .map(this::toFeatureResponse)
                .toList();
    }

    public TenantFeatureResponse upsertFeature(String tenantId, String featureKey, UpsertTenantFeatureRequest request) {
        requireTenant(tenantId);
        validateFeatureKey(featureKey);

        TenantFeature feature = new TenantFeature(
                tenantId,
                featureKey,
                request.enabled(),
                Instant.now(clock)
        );

        return toFeatureResponse(tenantFeatureRepository.save(feature));
    }

    private Tenant requireTenant(String tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
    }

    private void validateFeatureKey(String featureKey) {
        if (!SUPPORTED_FEATURE_KEYS.contains(featureKey)) {
            throw new IllegalArgumentException("Unsupported feature key: " + featureKey);
        }
    }

    private TenantResponse toTenantResponse(Tenant tenant) {
        return new TenantResponse(
                tenant.id(),
                tenant.key(),
                tenant.displayName(),
                tenant.status(),
                tenant.createdAt(),
                tenant.updatedAt()
        );
    }

    private TenantFeatureResponse toFeatureResponse(TenantFeature tenantFeature) {
        return new TenantFeatureResponse(
                tenantFeature.tenantId(),
                tenantFeature.featureKey(),
                tenantFeature.enabled(),
                tenantFeature.updatedAt()
        );
    }
}
