package io.lombardio.platform.tenant.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lombardio.platform.iam.application.KeycloakService;
import io.lombardio.platform.integration.application.PlatformOutboxService;
import io.lombardio.platform.tenant.api.CreateTenantRequest;
import io.lombardio.platform.tenant.api.CreateTenantUserRequest;
import io.lombardio.platform.tenant.api.TenantFeatureResponse;
import io.lombardio.platform.tenant.api.TenantResponse;
import io.lombardio.platform.tenant.api.TenantUserResponse;
import io.lombardio.platform.tenant.api.UpdateTenantRequest;
import io.lombardio.platform.tenant.api.UpsertTenantFeatureRequest;
import io.lombardio.platform.tenant.domain.Tenant;
import io.lombardio.platform.tenant.domain.TenantFeature;
import io.lombardio.platform.tenant.domain.TenantFeatureRepository;
import io.lombardio.platform.tenant.domain.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
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
    private final PlatformOutboxService platformOutboxService;
    private final KeycloakService keycloakService;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public TenantCatalogService(
            TenantRepository tenantRepository,
            TenantFeatureRepository tenantFeatureRepository,
            PlatformOutboxService platformOutboxService,
            KeycloakService keycloakService,
            ObjectMapper objectMapper,
            Clock clock
    ) {
        this.tenantRepository = tenantRepository;
        this.tenantFeatureRepository = tenantFeatureRepository;
        this.platformOutboxService = platformOutboxService;
        this.keycloakService = keycloakService;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    public List<TenantResponse> listTenants() {
        return tenantRepository.findAll().stream().map(this::toTenantResponse).toList();
    }

    @Transactional
    public TenantResponse createTenant(CreateTenantRequest request) {
        tenantRepository.findByKey(request.key()).ifPresent(tenant -> {
            throw new IllegalArgumentException("Tenant key already exists: " + request.key());
        });

        Instant now = Instant.now(clock);
        String tenantId = "tenant-" + UUID.randomUUID();
        Tenant tenant = new Tenant(
                tenantId,
                request.key(),
                request.displayName(),
                request.status(),
                now,
                now
        );

        // Create Keycloak group for tenant
        keycloakService.createTenantGroup(tenantId, request.displayName());

        Tenant saved = tenantRepository.save(tenant);
        recordTenantEvent("platform.tenant.created", saved);
        return toTenantResponse(saved);
    }

    public TenantUserResponse createTenantUser(String tenantId, CreateTenantUserRequest request) {
        requireTenant(tenantId);
        String userId = keycloakService.createTenantUser(
                tenantId,
                request.email(),
                request.password(),
                request.roles()
        );
        return new TenantUserResponse(userId, request.email(), request.displayName(), request.roles());
    }

    public List<String> listAvailableRoles() {
        return keycloakService.getAvailableRoles();
    }

    @Transactional
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

        Tenant saved = tenantRepository.save(updated);
        recordTenantEvent("platform.tenant.updated", saved);
        return toTenantResponse(saved);
    }

    public List<TenantFeatureResponse> listFeatures(String tenantId) {
        requireTenant(tenantId);
        return tenantFeatureRepository.findByTenantId(tenantId).stream()
                .map(this::toFeatureResponse)
                .toList();
    }

    @Transactional
    public TenantFeatureResponse upsertFeature(String tenantId, String featureKey, UpsertTenantFeatureRequest request) {
        requireTenant(tenantId);
        validateFeatureKey(featureKey);

        TenantFeature feature = new TenantFeature(
                tenantId,
                featureKey,
                request.enabled(),
                Instant.now(clock)
        );

        TenantFeature saved = tenantFeatureRepository.save(feature);
        recordFeatureEvent(request.enabled() ? "platform.tenant.feature.enabled" : "platform.tenant.feature.disabled", saved);
        return toFeatureResponse(saved);
    }

    private void recordTenantEvent(String eventType, Tenant tenant) {
        platformOutboxService.record(
                "tenant",
                tenant.id(),
                eventType,
                tenant.id(),
                toJson(Map.of(
                        "tenantId", tenant.id(),
                        "key", tenant.key(),
                        "displayName", tenant.displayName(),
                        "status", tenant.status(),
                        "createdAt", tenant.createdAt().toString(),
                        "updatedAt", tenant.updatedAt().toString()
                ))
        );
    }

    private void recordFeatureEvent(String eventType, TenantFeature feature) {
        platformOutboxService.record(
                "tenant-feature",
                feature.tenantId() + ":" + feature.featureKey(),
                eventType,
                feature.tenantId(),
                toJson(Map.of(
                        "tenantId", feature.tenantId(),
                        "featureKey", feature.featureKey(),
                        "enabled", feature.enabled(),
                        "updatedAt", feature.updatedAt().toString()
                ))
        );
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize outbox payload", exception);
        }
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
