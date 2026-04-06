package io.lombardio.identity.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(
    packages = "io.lombardio.identity",
    importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {
  @ArchTest
  static final ArchRule domainDoesNotDependOnApi =
      noClasses()
          .that()
          .resideInAnyPackage(
              "io.lombardio.identity..domain..",
              "io.lombardio.identity.aml..domain..",
              "io.lombardio.identity.kyc..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "io.lombardio.identity..api..",
              "io.lombardio.identity.aml..api..",
              "io.lombardio.identity.kyc..api..",
              "io.lombardio.identity.portal..api..");

  @ArchTest
  static final ArchRule domainDoesNotDependOnInfrastructure =
      noClasses()
          .that()
          .resideInAnyPackage(
              "io.lombardio.identity..domain..",
              "io.lombardio.identity.aml..domain..",
              "io.lombardio.identity.kyc..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "io.lombardio.identity..infrastructure..",
              "io.lombardio.identity.aml..infrastructure..",
              "io.lombardio.identity.kyc..infrastructure..",
              "io.lombardio.identity.portal..infrastructure..");

  @ArchTest
  static final ArchRule applicationDoesNotDependOnApi =
      noClasses()
          .that()
          .resideInAnyPackage(
              "io.lombardio.identity..application..",
              "io.lombardio.identity.aml..application..",
              "io.lombardio.identity.kyc..application..",
              "io.lombardio.identity.portal..application..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "io.lombardio.identity..api..",
              "io.lombardio.identity.aml..api..",
              "io.lombardio.identity.kyc..api..",
              "io.lombardio.identity.portal..api..");

  @ArchTest
  static final ArchRule applicationDoesNotDependOnInfrastructure =
      noClasses()
          .that()
          .resideInAnyPackage(
              "io.lombardio.identity..application..",
              "io.lombardio.identity.aml..application..",
              "io.lombardio.identity.kyc..application..",
              "io.lombardio.identity.portal..application..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "io.lombardio.identity..infrastructure..",
              "io.lombardio.identity.aml..infrastructure..",
              "io.lombardio.identity.kyc..infrastructure..",
              "io.lombardio.identity.portal..infrastructure..");

  @ArchTest
  static final ArchRule infrastructureDoesNotDependOnApi =
      noClasses()
          .that()
          .resideInAnyPackage(
              "io.lombardio.identity..infrastructure..",
              "io.lombardio.identity.aml..infrastructure..",
              "io.lombardio.identity.kyc..infrastructure..",
              "io.lombardio.identity.portal..infrastructure..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "io.lombardio.identity..api..",
              "io.lombardio.identity.aml..api..",
              "io.lombardio.identity.kyc..api..",
              "io.lombardio.identity.portal..api..");

  private HexagonalArchitectureTest() {}
}
