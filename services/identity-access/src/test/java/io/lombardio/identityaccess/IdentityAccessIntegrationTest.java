package io.lombardio.identityaccess;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IdentityAccessIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRejectProtectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldLoginAndFetchProtectedResources() throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "platform@lombardio.local",
                                  "password": "change-me"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AUTHENTICATED"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.userId").value("user-platform-admin"))
                .andExpect(jsonPath("$.tenantId").value("tenant-platform"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = extractToken(response);

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value("tenant-platform"))
                .andExpect(jsonPath("$.email").value("platform@lombardio.local"))
                .andExpect(jsonPath("$.mfaEnabled").value(false))
                .andExpect(jsonPath("$.roles[0]").value("platform-admin"));

        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].username", hasItem("admin")))
                .andExpect(jsonPath("$[*].username", hasItem("platform-admin")));

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent())
                .andExpect(header().doesNotExist("Set-Cookie"));

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldEnrollAndRequireTotpForSubsequentLogin() throws Exception {
        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@lombardio.local",
                                  "password": "change-me"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String accessToken = extractToken(loginResponse);

        String enrollmentResponse = mockMvc.perform(post("/api/v1/auth/mfa/totp/enroll")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.secret", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String secret = extractJsonValue(enrollmentResponse, "secret");
        String code = new io.lombardio.identityaccess.auth.application.TotpCodeService(
                java.time.Clock.systemUTC()
        ).currentCode(secret);

        mockMvc.perform(post("/api/v1/auth/mfa/totp/activate")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "%s"
                                }
                                """.formatted(code)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mfaEnabled").value(true))
                .andExpect(jsonPath("$.mfaMethods[0]").value("TOTP"));

        String challengeResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@lombardio.local",
                                  "password": "change-me"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("MFA_REQUIRED"))
                .andExpect(jsonPath("$.challengeId", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String challengeId = extractJsonValue(challengeResponse, "challengeId");

        mockMvc.perform(post("/api/v1/auth/mfa/totp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "challengeId": "%s",
                                  "code": "%s"
                                }
                                """.formatted(challengeId, code)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AUTHENTICATED"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    private String extractToken(String response) {
        return extractJsonValue(response, "accessToken");
    }

    private String extractJsonValue(String response, String field) {
        String marker = "\"" + field + "\":\"";
        int start = response.indexOf(marker);
        int from = start + marker.length();
        int to = response.indexOf('"', from);
        return response.substring(from, to);
    }
}
