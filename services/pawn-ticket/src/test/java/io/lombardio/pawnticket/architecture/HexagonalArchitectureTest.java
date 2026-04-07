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
package io.lombardio.pawnticket.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
    packages = "io.lombardio.pawnticket",
    importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

  @ArchTest
  static final ArchRule domainDoesNotDependOnAdapters =
      noClasses()
          .that()
          .resideInAnyPackage("io.lombardio.pawnticket.domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "io.lombardio.pawnticket.api..",
              "io.lombardio.pawnticket.infrastructure..",
              "io.lombardio.pawnticket.security..");

  @ArchTest
  static final ArchRule applicationDoesNotDependOnAdapters =
      noClasses()
          .that()
          .resideInAnyPackage("io.lombardio.pawnticket.application..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "io.lombardio.pawnticket.api..",
              "io.lombardio.pawnticket.infrastructure..",
              "io.lombardio.pawnticket.security..");

  @ArchTest
  static final ArchRule infrastructureDoesNotDependOnApi =
      noClasses()
          .that()
          .resideInAnyPackage(
              "io.lombardio.pawnticket.infrastructure..", "io.lombardio.pawnticket.security..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("io.lombardio.pawnticket.api..");

  private HexagonalArchitectureTest() {}
}
