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
package io.lombardio.identity.bootstrap;

import io.lombardio.identity.aml.bootstrap.AmlDevelopmentSeeder;
import io.lombardio.identity.kyc.bootstrap.KycDevelopmentSeeder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("bootstrap")
public class IdentityIntelligenceBootstrap implements CommandLineRunner {

  private final CustomerDevelopmentSeeder customerSeeder;
  private final KycDevelopmentSeeder kycSeeder;
  private final AmlDevelopmentSeeder amlSeeder;

  public IdentityIntelligenceBootstrap(
      CustomerDevelopmentSeeder customerSeeder,
      KycDevelopmentSeeder kycSeeder,
      AmlDevelopmentSeeder amlSeeder) {
    this.customerSeeder = customerSeeder;
    this.kycSeeder = kycSeeder;
    this.amlSeeder = amlSeeder;
  }

  @Override
  public void run(String... args) {
    System.out.println("[BOOTSTRAP] Seeding Identity Intelligence data...");

    try {
      customerSeeder.seed();
      System.out.println("[BOOTSTRAP] Customers seeded.");

      kycSeeder.seed();
      System.out.println("[BOOTSTRAP] KYC status seeded.");

      amlSeeder.seed();
      System.out.println("[BOOTSTRAP] AML cases seeded.");

    } catch (Exception e) {
      System.err.println("[BOOTSTRAP] Error during seeding: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
