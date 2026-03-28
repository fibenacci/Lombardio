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
package io.lombardio.identity.infrastructure.http;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

public class WebhookExternalCrmConnectorTest {

  @Test
  public void testConnectorDisabledWhenUrlEmpty() {
    RestClient.Builder builder = mock(RestClient.Builder.class);
    WebhookExternalCrmConnector connector = new WebhookExternalCrmConnector(builder, "");

    assertFalse(connector.supports("any-tenant"));
    assertTrue(connector.search("any-tenant", "query").isEmpty());
  }

  @Test
  public void testConnectorEnabledWhenUrlPresent() {
    RestClient.Builder builder = mock(RestClient.Builder.class);
    when(builder.build()).thenReturn(mock(RestClient.class));
    WebhookExternalCrmConnector connector =
        new WebhookExternalCrmConnector(builder, "http://crm.local");

    assertTrue(connector.supports("any-tenant"));
  }
}
