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

import static org.mockito.Mockito.mock;

import io.lombardio.platform.bff.application.OperatorBffProxyService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

class OperatorPawnTicketFacadeControllerTest extends OperatorFacadeControllerTestSupport {

  @Test
  void forwardsPawnTicketListToPawnTicketService() {
    OperatorBffProxyService proxyService = mock(OperatorBffProxyService.class);
    OperatorPawnTicketFacadeController controller =
        new OperatorPawnTicketFacadeController(proxyService);
    MockHttpServletRequest request =
        new MockHttpServletRequest(
            "GET", "/api/v1/platform/operator/tenants/tenant-default/pawn-tickets");
    byte[] responseBody = "[]".getBytes();

    stubForward(
        proxyService,
        "pawn-ticket",
        "/api/v1/tenants/tenant-default/pawn-tickets",
        null,
        HttpMethod.GET,
        null,
        responseBody);

    ResponseEntity<byte[]> response = controller.listPawnTickets("tenant-default", request);

    assertForwarded(
        response,
        responseBody,
        proxyService,
        "pawn-ticket",
        "/api/v1/tenants/tenant-default/pawn-tickets",
        null,
        HttpMethod.GET,
        null);
  }

  @Test
  void forwardsCashTransactionCreationToPawnTicketService() {
    OperatorBffProxyService proxyService = mock(OperatorBffProxyService.class);
    OperatorPawnTicketFacadeController controller =
        new OperatorPawnTicketFacadeController(proxyService);
    byte[] requestBody = "{\"ticketNumber\":\"PS-1001\"}".getBytes();
    byte[] responseBody = "{\"id\":\"cash-1\"}".getBytes();
    MockHttpServletRequest request =
        new MockHttpServletRequest("POST", "/api/v1/platform/operator/cash-transactions");

    stubForward(
        proxyService,
        "pawn-ticket",
        "/api/v1/cash-transactions",
        null,
        HttpMethod.POST,
        requestBody,
        responseBody);

    ResponseEntity<byte[]> response = controller.createCashTransaction(request, requestBody);

    assertForwarded(
        response,
        responseBody,
        proxyService,
        "pawn-ticket",
        "/api/v1/cash-transactions",
        null,
        HttpMethod.POST,
        requestBody);
  }
}
