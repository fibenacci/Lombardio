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
package io.lombardio.platform.bff.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.lombardio.platform.bff.application.OperatorBffProxyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GenericOperatorFacadeController.class)
@AutoConfigureMockMvc(addFilters = false)
class GenericOperatorFacadeControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private OperatorBffProxyService proxyService;

  @Test
  void forwardsGetRequestToResolvedServiceUsingPathAsKey() throws Exception {
    when(proxyService.forward(
            eq("auctions"),
            eq("/api/v1/tenants/tenant-1/auctions"),
            any(),
            eq(HttpMethod.GET),
            any(),
            any()))
        .thenReturn(ResponseEntity.ok(new byte[0]));

    mockMvc
        .perform(get("/api/v1/platform/operator/tenants/tenant-1/auctions"))
        .andExpect(status().isOk());

    verify(proxyService)
        .forward(
            eq("auctions"),
            eq("/api/v1/tenants/tenant-1/auctions"),
            any(),
            eq(HttpMethod.GET),
            any(),
            any());
  }

  @Test
  void forwardsPostRequestToResolvedServiceUsingPathAsKey() throws Exception {
    byte[] body = "{}".getBytes();
    when(proxyService.forward(
            eq("customers"),
            eq("/api/v1/tenants/tenant-1/customers"),
            any(),
            eq(HttpMethod.POST),
            eq(body),
            any()))
        .thenReturn(ResponseEntity.ok(new byte[0]));

    mockMvc
        .perform(post("/api/v1/platform/operator/tenants/tenant-1/customers").content(body))
        .andExpect(status().isOk());

    verify(proxyService)
        .forward(
            eq("customers"),
            eq("/api/v1/tenants/tenant-1/customers"),
            any(),
            eq(HttpMethod.POST),
            eq(body),
            any());
  }
}
