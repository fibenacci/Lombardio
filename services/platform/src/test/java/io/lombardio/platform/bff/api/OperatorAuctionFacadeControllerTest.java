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

class OperatorAuctionFacadeControllerTest extends OperatorFacadeControllerTestSupport {

  @Test
  void forwardsAuctionListToAuctionService() {
    OperatorBffProxyService proxyService = mock(OperatorBffProxyService.class);
    OperatorAuctionFacadeController controller = new OperatorAuctionFacadeController(proxyService);
    MockHttpServletRequest request =
        new MockHttpServletRequest(
            "GET", "/api/v1/platform/operator/tenants/tenant-default/auctions");
    byte[] responseBody = "[]".getBytes();

    stubForward(
        proxyService,
        "auction",
        "/api/v1/tenants/tenant-default/auctions",
        null,
        HttpMethod.GET,
        null,
        responseBody);

    ResponseEntity<byte[]> response = controller.listAuctions("tenant-default", request);

    assertForwarded(
        response,
        responseBody,
        proxyService,
        "auction",
        "/api/v1/tenants/tenant-default/auctions",
        null,
        HttpMethod.GET,
        null);
  }

  @Test
  void forwardsSettlementToAuctionService() {
    OperatorBffProxyService proxyService = mock(OperatorBffProxyService.class);
    OperatorAuctionFacadeController controller = new OperatorAuctionFacadeController(proxyService);
    byte[] requestBody = "{\"hammerPrice\":200}".getBytes();
    byte[] responseBody = "{\"id\":\"auction-1\"}".getBytes();
    MockHttpServletRequest request =
        new MockHttpServletRequest(
            "POST",
            "/api/v1/platform/operator/tenants/tenant-default/auctions/auction-1/lots/lot-1/settle");

    stubForward(
        proxyService,
        "auction",
        "/api/v1/tenants/tenant-default/auctions/auction-1/lots/lot-1/settle",
        null,
        HttpMethod.POST,
        requestBody,
        responseBody);

    ResponseEntity<byte[]> response =
        controller.settleLot("tenant-default", "auction-1", "lot-1", request, requestBody);

    assertForwarded(
        response,
        responseBody,
        proxyService,
        "auction",
        "/api/v1/tenants/tenant-default/auctions/auction-1/lots/lot-1/settle",
        null,
        HttpMethod.POST,
        requestBody);
  }
}
