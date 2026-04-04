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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class OperatorBffPropertiesTest {

  @Test
  void acceptsNonBlankTargets() {
    assertDoesNotThrow(
        () ->
            new OperatorBffProperties(
                "http://identity",
                "http://origination",
                "http://pawn-ticket",
                "http://auction",
                "http://online-auction",
                "http://reporting"));
  }

  @Test
  void resolvesConfiguredAliases() {
    OperatorBffProperties properties =
        new OperatorBffProperties(
            "http://identity",
            "http://origination",
            "http://pawn-ticket",
            "http://auction",
            "http://online-auction",
            "http://reporting");

    assertEquals("http://identity", properties.resolve("identity").orElseThrow());
    assertEquals("http://pawn-ticket", properties.resolve("pawn-ticket").orElseThrow());
    assertEquals("http://reporting", properties.resolve("reporting").orElseThrow());
  }

  @Test
  void rejectsBlankTarget() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new OperatorBffProperties(
                "",
                "http://origination",
                "http://pawn-ticket",
                "http://auction",
                "http://online-auction",
                "http://reporting"));
  }
}
