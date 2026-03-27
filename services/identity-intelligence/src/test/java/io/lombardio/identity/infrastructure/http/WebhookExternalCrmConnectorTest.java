package io.lombardio.identity.infrastructure.http;

import io.lombardio.identity.domain.model.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        WebhookExternalCrmConnector connector = new WebhookExternalCrmConnector(builder, "http://crm.local");
        
        assertTrue(connector.supports("any-tenant"));
    }
}
