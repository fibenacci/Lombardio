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
package io.lombardio.identity.aml.bootstrap;

import io.lombardio.identity.aml.domain.model.AmlCase;
import io.lombardio.identity.aml.domain.model.AmlRiskLevel;
import io.lombardio.identity.aml.domain.model.AmlStatus;
import io.lombardio.identity.aml.domain.port.AmlRepository;
import io.lombardio.identity.demo.DemoDataProperties;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AmlDevelopmentSeeder {

  private record DemoTenant(String id, String key) {}

  private static final List<DemoTenant> TENANTS =
      List.of(
          new DemoTenant("tenant-default", "default"),
          new DemoTenant("tenant-hamburg", "hanseatic"),
          new DemoTenant("tenant-munich", "isar"),
          new DemoTenant("tenant-cologne", "rhein"),
          new DemoTenant("tenant-stuttgart", "neckar"));

  private final AmlRepository amlRepository;
  private final DemoDataProperties demoDataProperties;

  AmlDevelopmentSeeder(AmlRepository amlRepository, DemoDataProperties demoDataProperties) {
    this.amlRepository = amlRepository;
    this.demoDataProperties = demoDataProperties;
  }

  public void seed() {
    int tenantCount = tenantCount(demoDataProperties.effectiveScale());
    int customersPerTenant = customersPerTenant(demoDataProperties.effectiveScale());
    Instant now = Instant.now();

    for (int tenantIndex = 0; tenantIndex < tenantCount; tenantIndex++) {
      DemoTenant tenant = TENANTS.get(tenantIndex);
      for (int customerIndex = 1; customerIndex <= customersPerTenant; customerIndex++) {
        amlRepository.save(
            buildCase(
                tenant,
                customerIndex,
                now.minusSeconds((long) (customerIndex + tenantIndex * 20) * 86_400L)));
      }
    }
  }

  private AmlCase buildCase(DemoTenant tenant, int customerIndex, Instant createdAt) {
    AmlStatus status =
        switch (customerIndex % 10) {
          case 0 -> AmlStatus.BLOCKED;
          case 1, 6 -> AmlStatus.REVIEW_REQUIRED;
          case 2 -> AmlStatus.REPORTED;
          case 3 -> AmlStatus.NOT_REVIEWED;
          default -> AmlStatus.CLEAR;
        };
    AmlRiskLevel riskLevel =
        switch (customerIndex % 7) {
          case 0, 1 -> AmlRiskLevel.HIGH;
          case 2, 3 -> AmlRiskLevel.MEDIUM;
          default -> AmlRiskLevel.LOW;
        };
    Instant reviewedAt =
        status == AmlStatus.CLEAR || status == AmlStatus.REPORTED || status == AmlStatus.BLOCKED
            ? createdAt.plusSeconds(7_200)
            : null;

    return new AmlCase(
        "aml-" + tenant.key() + "-" + String.format("%04d", customerIndex),
        tenant.id(),
        "customer-" + tenant.key() + "-" + String.format("%04d", customerIndex),
        status,
        riskLevel,
        customerIndex % 9 == 0,
        customerIndex % 5 == 0,
        customerIndex % 4 == 0,
        customerIndex % 3 == 0,
        customerIndex % 11 == 0,
        customerIndex % 11 == 0 ? "Meldepaket vorbereitet" : null,
        noteFor(status, riskLevel),
        createdAt,
        reviewedAt,
        createdAt);
  }

  private String noteFor(AmlStatus status, AmlRiskLevel riskLevel) {
    return switch (status) {
      case CLEAR -> "Regulaere AML-Pruefung ohne Auffaelligkeit, Risikostufe " + riskLevel.name();
      case REVIEW_REQUIRED -> "Manuelle Vertiefungspruefung wegen Auffaelligkeiten im Kundenprofil";
      case BLOCKED ->
          "Auszahlung gesperrt, bis wirtschaftlich Berechtigte abschliessend geprueft sind";
      case REPORTED -> "Sachverhalt intern eskaliert und als meldepflichtig markiert";
      case NOT_REVIEWED -> "Automatische Vorpruefung vorhanden, manuelle Sichtung noch offen";
    };
  }

  private int tenantCount(String scale) {
    return switch (normalize(scale)) {
      case "small" -> 2;
      case "large" -> TENANTS.size();
      default -> 4;
    };
  }

  private int customersPerTenant(String scale) {
    return switch (normalize(scale)) {
      case "small" -> 12;
      case "large" -> 90;
      default -> 36;
    };
  }

  private String normalize(String scale) {
    return scale == null ? "medium" : scale.trim().toLowerCase();
  }
}
