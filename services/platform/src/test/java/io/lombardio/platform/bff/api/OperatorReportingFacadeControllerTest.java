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

class OperatorReportingFacadeControllerTest extends OperatorFacadeControllerTestSupport {

  @Test
  void forwardsDashboardOverviewToReportingService() {
    OperatorBffProxyService proxyService = mock(OperatorBffProxyService.class);
    OperatorReportingFacadeController controller =
        new OperatorReportingFacadeController(proxyService);
    MockHttpServletRequest request =
        new MockHttpServletRequest(
            "GET", "/api/v1/platform/operator/tenants/tenant-default/reporting/dashboard");
    request.setQueryString("rangeDays=14");
    byte[] responseBody = "{\"totals\":{}}".getBytes();

    stubForward(
        proxyService,
        "reporting",
        "/api/v1/tenants/tenant-default/reporting/dashboard",
        "rangeDays=14",
        HttpMethod.GET,
        null,
        responseBody);

    ResponseEntity<byte[]> response = controller.dashboard("tenant-default", request);

    assertForwarded(
        response,
        responseBody,
        proxyService,
        "reporting",
        "/api/v1/tenants/tenant-default/reporting/dashboard",
        "rangeDays=14",
        HttpMethod.GET,
        null);
  }
}
