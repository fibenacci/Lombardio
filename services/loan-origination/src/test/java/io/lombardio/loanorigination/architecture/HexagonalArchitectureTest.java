package io.lombardio.loanorigination.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
    packages = "io.lombardio.loanorigination",
    importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

  @ArchTest
  static final ArchRule domainDoesNotDependOnAdapters =
      noClasses()
          .that()
          .resideInAnyPackage("io.lombardio.loanorigination.domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "io.lombardio.loanorigination.api..",
              "io.lombardio.loanorigination.infrastructure..",
              "io.lombardio.loanorigination.security..");

  @ArchTest
  static final ArchRule applicationDoesNotDependOnAdapters =
      noClasses()
          .that()
          .resideInAnyPackage("io.lombardio.loanorigination.application..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "io.lombardio.loanorigination.api..",
              "io.lombardio.loanorigination.infrastructure..",
              "io.lombardio.loanorigination.security..");

  @ArchTest
  static final ArchRule infrastructureDoesNotDependOnApi =
      noClasses()
          .that()
          .resideInAnyPackage(
              "io.lombardio.loanorigination.infrastructure..",
              "io.lombardio.loanorigination.security..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("io.lombardio.loanorigination.api..");

  private HexagonalArchitectureTest() {}
}
