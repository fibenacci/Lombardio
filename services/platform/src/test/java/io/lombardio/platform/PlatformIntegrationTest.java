package io.lombardio.platform;

import io.lombardio.platform.config.TestIdentityAccessClientConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(TestIdentityAccessClientConfig.class)
class PlatformIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldListSeededTenant() throws Exception {
        mockMvc.perform(get("/api/v1/platform/tenants")
                        .header("Authorization", "Bearer platform-read-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("tenant-default"))
                .andExpect(jsonPath("$[0].key").value("default"));
    }

    @Test
    void shouldCreateTenantAndUpsertFeature() throws Exception {
        String response = mockMvc.perform(post("/api/v1/platform/tenants")
                        .header("Authorization", "Bearer platform-write-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "key": "alpha",
                                  "displayName": "Pfandhaus Alpha",
                                  "status": "ACTIVE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value("alpha"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String tenantId = extractField(response, "id");

        mockMvc.perform(put("/api/v1/platform/tenants/{id}/features/{featureKey}", tenantId, "customer-management")
                        .header("Authorization", "Bearer platform-write-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "enabled": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value(tenantId))
                .andExpect(jsonPath("$.featureKey").value("customer-management"))
                .andExpect(jsonPath("$.enabled").value(true));

        mockMvc.perform(post("/internal/v1/outbox-events/claim")
                        .header("Authorization", "Bearer test-platform-outbox-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "consumer": "integration-service",
                                  "limit": 10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventType").value("platform.tenant.created"))
                .andExpect(jsonPath("$[1].eventType").value("platform.tenant.feature.enabled"));
    }

    @Test
    void shouldRejectUnauthenticatedPlatformRequest() throws Exception {
        mockMvc.perform(get("/api/v1/platform/tenants"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectTenantAdminWithoutPlatformPermission() throws Exception {
        mockMvc.perform(get("/api/v1/platform/tenants")
                        .header("Authorization", "Bearer tenant-admin-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowTenantAdminToReadOwnTenantFeatures() throws Exception {
        mockMvc.perform(get("/api/v1/platform/tenants/{id}/features", "tenant-default")
                        .header("Authorization", "Bearer tenant-admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tenantId").value("tenant-default"));
    }

    @Test
    void shouldRejectTenantAdminWhenReadingForeignTenantFeatures() throws Exception {
        mockMvc.perform(get("/api/v1/platform/tenants/{id}/features", "tenant-other")
                        .header("Authorization", "Bearer tenant-admin-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectDelegatedSessionForPlatformMutation() throws Exception {
        mockMvc.perform(post("/api/v1/platform/tenants")
                        .header("Authorization", "Bearer delegated-platform-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "key": "beta",
                                  "displayName": "Pfandhaus Beta",
                                  "status": "ACTIVE"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectInternalOutboxWithoutToken() throws Exception {
        mockMvc.perform(post("/internal/v1/outbox-events/claim")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "consumer": "integration-service",
                                  "limit": 10
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    private String extractField(String response, String field) {
        String marker = "\"" + field + "\":\"";
        int start = response.indexOf(marker);
        int from = start + marker.length();
        int to = response.indexOf('"', from);
        return response.substring(from, to);
    }
}
