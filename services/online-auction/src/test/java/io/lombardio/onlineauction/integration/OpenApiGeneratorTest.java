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
package io.lombardio.onlineauction.integration;

import io.lombardio.platform.security.test.AbstractOpenApiGenerator;
import io.lombardio.onlineauction.application.OnlineAuctionMetrics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiGeneratorTest extends AbstractOpenApiGenerator {

  @Autowired private MockMvc mockMvc;

  @MockBean private OnlineAuctionMetrics metrics;

  @Test
  void generateOpenApiSpec() throws Exception {
    generateSpec(mockMvc, "online-auction");
  }
}
