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
              "io.lombardio.pawnticket.infrastructure..",
              "io.lombardio.pawnticket.security..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("io.lombardio.pawnticket.api..");

  private HexagonalArchitectureTest() {}
}
