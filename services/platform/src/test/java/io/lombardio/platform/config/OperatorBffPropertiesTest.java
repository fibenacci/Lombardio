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
package io.lombardio.platform.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class OperatorBffPropertiesTest {

  @Test
  void resolvesConfiguredTargets() {
    OperatorBffProperties properties =
        new OperatorBffProperties(
            Map.of(
                "identity", "http://identity",
                "auction", "http://auction"));

    assertEquals("http://identity", properties.resolve("identity").orElseThrow());
    assertEquals("http://auction", properties.resolve("auction").orElseThrow());
    assertTrue(properties.resolve("unknown").isEmpty());
  }

  @Test
  void handlesEmptyTargets() {
    OperatorBffProperties properties = new OperatorBffProperties(Map.of());
    assertTrue(properties.resolve("any").isEmpty());
  }
}
