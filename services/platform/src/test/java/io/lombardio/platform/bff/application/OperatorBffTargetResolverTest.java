package io.lombardio.platform.bff.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.lombardio.platform.config.OperatorBffProperties;
import org.junit.jupiter.api.Test;

class OperatorBffTargetResolverTest {

  @Test
  void resolvesConfiguredTargetUri() {
    OperatorBffTargetResolver resolver = new OperatorBffTargetResolver(properties());

    String resolved =
        resolver.resolve("reporting", "/api/v1/tenants/tenant-default/reporting/dashboard", "rangeDays=14")
            .toString();

    assertEquals(
        "http://reporting:8091/api/v1/tenants/tenant-default/reporting/dashboard?rangeDays=14",
        resolved);
  }

  @Test
  void rejectsUnknownServiceKey() {
    OperatorBffTargetResolver resolver = new OperatorBffTargetResolver(properties());

    assertThrows(
        IllegalArgumentException.class,
        () -> resolver.resolve("unknown", "/api/v1/test", null));
  }

  private OperatorBffProperties properties() {
    return new OperatorBffProperties(
        "http://identity:8084",
        "http://origination:8083",
        "http://pawn-ticket:8085",
        "http://auction:8089",
        "http://online-auction:8090",
        "http://reporting:8091");
  }
}
