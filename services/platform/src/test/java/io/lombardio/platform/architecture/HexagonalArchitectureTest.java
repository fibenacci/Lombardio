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
package io.lombardio.platform.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
    packages = "io.lombardio.platform",
    importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

  @ArchTest
  static final ArchRule domainDoesNotDependOnApi =
      noClasses()
          .that()
          .resideInAnyPackage(
              "io.lombardio.platform.integration.domain..", "io.lombardio.platform.tenant.domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "io.lombardio.platform.auth.api..",
              "io.lombardio.platform.bff.api..",
              "io.lombardio.platform.integration.api..",
              "io.lombardio.platform.permission.api..",
              "io.lombardio.platform.shared.api..",
              "io.lombardio.platform.tenant.api..");

  @ArchTest
  static final ArchRule domainDoesNotDependOnInfrastructure =
      noClasses()
          .that()
          .resideInAnyPackage(
              "io.lombardio.platform.integration.domain..", "io.lombardio.platform.tenant.domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "io.lombardio.platform.auth.infrastructure..",
              "io.lombardio.platform.iam.infrastructure..",
              "io.lombardio.platform.infrastructure..",
              "io.lombardio.platform.integration.infrastructure..");

  @ArchTest
  static final ArchRule applicationDoesNotDependOnApi =
      noClasses()
          .that()
          .resideInAnyPackage(
              "io.lombardio.platform.auth.application..",
              "io.lombardio.platform.bff.application..",
              "io.lombardio.platform.iam.application..",
              "io.lombardio.platform.integration.application..",
              "io.lombardio.platform.permission.application..",
              "io.lombardio.platform.tenant.application..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "io.lombardio.platform.auth.api..",
              "io.lombardio.platform.bff.api..",
              "io.lombardio.platform.integration.api..",
              "io.lombardio.platform.permission.api..",
              "io.lombardio.platform.shared.api..",
              "io.lombardio.platform.tenant.api..");

  @ArchTest
  static final ArchRule applicationDoesNotDependOnInfrastructure =
      noClasses()
          .that()
          .resideInAnyPackage(
              "io.lombardio.platform.auth.application..",
              "io.lombardio.platform.bff.application..",
              "io.lombardio.platform.iam.application..",
              "io.lombardio.platform.integration.application..",
              "io.lombardio.platform.permission.application..",
              "io.lombardio.platform.tenant.application..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "io.lombardio.platform.auth.infrastructure..",
              "io.lombardio.platform.iam.infrastructure..",
              "io.lombardio.platform.infrastructure..",
              "io.lombardio.platform.integration.infrastructure..");

  @ArchTest
  static final ArchRule infrastructureDoesNotDependOnApi =
      noClasses()
          .that()
          .resideInAnyPackage(
              "io.lombardio.platform.auth.infrastructure..",
              "io.lombardio.platform.iam.infrastructure..",
              "io.lombardio.platform.infrastructure..",
              "io.lombardio.platform.integration.infrastructure..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "io.lombardio.platform.auth.api..",
              "io.lombardio.platform.bff.api..",
              "io.lombardio.platform.integration.api..",
              "io.lombardio.platform.permission.api..",
              "io.lombardio.platform.shared.api..",
              "io.lombardio.platform.tenant.api..");

  private HexagonalArchitectureTest() {}
}
