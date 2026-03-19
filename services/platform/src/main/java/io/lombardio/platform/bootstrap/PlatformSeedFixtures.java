package io.lombardio.platform.bootstrap;

import io.lombardio.platform.tenant.domain.Tenant;
import io.lombardio.platform.tenant.domain.TenantFeature;

import java.time.Instant;
import java.util.List;

public final class PlatformSeedFixtures {

    private PlatformSeedFixtures() {
    }

    public static Tenant defaultTenant() {
        Instant now = Instant.parse("2026-03-18T00:00:00Z");
        return new Tenant(
                "tenant-default",
                "default",
                "Default Tenant",
                "ACTIVE",
                now,
                now
        );
    }

    public static List<TenantFeature> defaultTenantFeatures() {
        Instant now = Instant.parse("2026-03-18T00:00:00Z");
        return List.of(
                new TenantFeature("tenant-default", "identity-access", true, now),
                new TenantFeature("tenant-default", "customer-management", false, now),
                new TenantFeature("tenant-default", "collateral-management", false, now),
                new TenantFeature("tenant-default", "aml-compliance", true, now),
                new TenantFeature("tenant-default", "kyc-provider-verification", false, now),
                new TenantFeature("tenant-default", "kyc-document-ocr", true, now),
                new TenantFeature("tenant-default", "auction-workflow", true, now),
                new TenantFeature("tenant-default", "online-auctions", true, now)
        );
    }
}
