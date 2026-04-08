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
package io.lombardio.platform.integration.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class IntegrationRabbitMqPropertiesTest {

  @Test
  void holdsConfigurationValues() {
    IntegrationRabbitMqProperties properties =
        new IntegrationRabbitMqProperties("exchange-1", 100, true);

    assertEquals("exchange-1", properties.exchange());
    assertEquals(100, properties.publisherBatchSize());
    assertTrue(properties.publisherEnabled());
  }
}
