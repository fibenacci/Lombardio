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
package io.lombardio.platform.demo;

import io.lombardio.platform.bootstrap.PlatformSeedFixtures;
import io.lombardio.platform.iam.application.KeycloakService;
import io.lombardio.platform.tenant.domain.BranchRepository;
import io.lombardio.platform.tenant.domain.TenantFeatureRepository;
import io.lombardio.platform.tenant.domain.TenantRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ScenarioDataSeeder {

  private static final List<PlatformSeedFixtures.DemoTenant> DEMO_TENANTS =
      List.of(
          new PlatformSeedFixtures.DemoTenant(
              "tenant-default",
              "default",
              "Default Tenant",
              "ACTIVE",
              true,
              true,
              true,
              true,
              true,
              true,
              true),
          new PlatformSeedFixtures.DemoTenant(
              "tenant-hamburg",
              "hanseatic",
              "Hanseatic Pawn Hamburg",
              "ACTIVE",
              true,
              true,
              true,
              true,
              true,
              true,
              true),
          new PlatformSeedFixtures.DemoTenant(
              "tenant-munich",
              "isar",
              "Isar Pfand Muenchen",
              "ACTIVE",
              true,
              true,
              true,
              true,
              true,
              true,
              true),
          new PlatformSeedFixtures.DemoTenant(
              "tenant-cologne",
              "rhein",
              "Rhein Pfand Koeln",
              "ACTIVE",
              true,
              true,
              true,
              false,
              true,
              false,
              false),
          new PlatformSeedFixtures.DemoTenant(
              "tenant-stuttgart",
              "neckar",
              "Neckar Pfand Stuttgart",
              "INACTIVE",
              true,
              false,
              true,
              false,
              false,
              false,
              false));

  private final TenantRepository tenantRepository;
  private final TenantFeatureRepository tenantFeatureRepository;
  private final BranchRepository branchRepository;
  private final KeycloakService keycloakService;

  public ScenarioDataSeeder(
      TenantRepository tenantRepository,
      TenantFeatureRepository tenantFeatureRepository,
      BranchRepository branchRepository,
      KeycloakService keycloakService) {
    this.tenantRepository = tenantRepository;
    this.tenantFeatureRepository = tenantFeatureRepository;
    this.branchRepository = branchRepository;
    this.keycloakService = keycloakService;
  }

  public void seed() {
    Instant timestamp = Instant.now().minusSeconds(86_400); // 24 hours ago

    for (var tenantDefinition : DEMO_TENANTS) {
      keycloakService.createTenantGroup(tenantDefinition.id(), tenantDefinition.displayName());
      tenantRepository.save(PlatformSeedFixtures.toTenant(tenantDefinition, timestamp));
      for (var feature : PlatformSeedFixtures.tenantFeatures(tenantDefinition, timestamp)) {
        tenantFeatureRepository.save(feature);
      }
    }
    PlatformSeedFixtures.defaultTenantBranches().forEach(branchRepository::save);
  }

  private static PlatformSeedFixtures.DemoTenant DEFAULT_TENANT() {
    return DEMO_TENANTS.get(0);
  }
}
