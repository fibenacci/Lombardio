package io.lombardio.platform.demo;

import io.lombardio.platform.tenant.domain.Tenant;
import io.lombardio.platform.tenant.domain.TenantFeature;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class PlatformSeedFixtures {

    public record DemoTenant(
            String id,
            String key,
            String displayName,
            String status,
            boolean customerManagement,
            boolean collateralManagement,
            boolean amlCompliance,
            boolean kycProviderVerification,
            boolean kycDocumentOcr,
            boolean auctionWorkflow,
            boolean onlineAuctions
    ) {
    }

    private static final List<DemoTenant> DEMO_TENANTS = List.of(
            new DemoTenant("tenant-default", "default", "Default Tenant", "ACTIVE", true, true, true, true, true, true, true),
            new DemoTenant("tenant-hamburg", "hanseatic", "Hanseatic Pawn Hamburg", "ACTIVE", true, true, true, true, true, true, true),
            new DemoTenant("tenant-munich", "isar", "Isar Pfand Muenchen", "ACTIVE", true, true, true, true, true, true, true),
            new DemoTenant("tenant-cologne", "rhein", "Rhein Pfand Koeln", "ACTIVE", true, true, true, false, true, false, false),
            new DemoTenant("tenant-stuttgart", "neckar", "Neckar Pfand Stuttgart", "INACTIVE", true, false, true, false, false, false, false)
    );

    private PlatformSeedFixtures() {
    }

    public static List<DemoTenant> tenantsForScale(String scale) {
        int count = switch (scale == null ? "medium" : scale.trim().toLowerCase()) {
            case "small" -> 2;
            case "large" -> DEMO_TENANTS.size();
            default -> 4;
        };
        return DEMO_TENANTS.subList(0, count);
    }

    public static Tenant toTenant(DemoTenant tenant, Instant timestamp) {
        return new Tenant(
                tenant.id(),
                tenant.key(),
                tenant.displayName(),
                tenant.status(),
                timestamp,
                timestamp
        );
    }

    public static List<TenantFeature> tenantFeatures(DemoTenant tenant, Instant timestamp) {
        List<TenantFeature> features = new ArrayList<>();
        features.add(new TenantFeature(tenant.id(), "identity-access", true, timestamp));
        features.add(new TenantFeature(tenant.id(), "customer-management", tenant.customerManagement(), timestamp));
        features.add(new TenantFeature(tenant.id(), "collateral-management", tenant.collateralManagement(), timestamp));
        features.add(new TenantFeature(tenant.id(), "aml-compliance", tenant.amlCompliance(), timestamp));
        features.add(new TenantFeature(tenant.id(), "kyc-provider-verification", tenant.kycProviderVerification(), timestamp));
        features.add(new TenantFeature(tenant.id(), "kyc-document-ocr", tenant.kycDocumentOcr(), timestamp));
        features.add(new TenantFeature(tenant.id(), "auction-workflow", tenant.auctionWorkflow(), timestamp));
        features.add(new TenantFeature(tenant.id(), "online-auctions", tenant.onlineAuctions(), timestamp));
        return features;
    }

    public static Tenant defaultTenant() {
        Instant timestamp = Instant.now().minusSeconds(86_400);
        return toTenant(DEFAULT_TENANT(), timestamp);
    }

    public static List<TenantFeature> defaultTenantFeatures() {
        Instant timestamp = Instant.now().minusSeconds(86_400);
        return tenantFeatures(DEFAULT_TENANT(), timestamp);
    }

    private static DemoTenant DEFAULT_TENANT() {
        return DEMO_TENANTS.get(0);
    }
}
