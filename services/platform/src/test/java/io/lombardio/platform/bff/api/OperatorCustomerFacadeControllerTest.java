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

class OperatorCustomerFacadeControllerTest extends OperatorFacadeControllerTestSupport {

  @Test
  void forwardsCustomerSearchToIdentityService() {
    OperatorBffProxyService proxyService = mock(OperatorBffProxyService.class);
    OperatorCustomerFacadeController controller =
        new OperatorCustomerFacadeController(proxyService);
    MockHttpServletRequest request =
        new MockHttpServletRequest(
            "GET", "/api/v1/platform/operator/tenants/tenant-default/customers");
    request.setQueryString("query=ben");
    byte[] responseBody = "[]".getBytes();

    stubForward(
        proxyService,
        "identity",
        "/api/v1/tenants/tenant-default/customers",
        "query=ben",
        HttpMethod.GET,
        null,
        responseBody);

    ResponseEntity<byte[]> response = controller.searchCustomers("tenant-default", request);

    assertForwarded(
        response,
        responseBody,
        proxyService,
        "identity",
        "/api/v1/tenants/tenant-default/customers",
        "query=ben",
        HttpMethod.GET,
        null);
  }

  @Test
  void forwardsKycPrefillToIdentityService() {
    OperatorBffProxyService proxyService = mock(OperatorBffProxyService.class);
    OperatorCustomerFacadeController controller =
        new OperatorCustomerFacadeController(proxyService);
    byte[] requestBody = "{\"imageDataUrl\":\"data:image/jpeg;base64,abc\"}".getBytes();
    byte[] responseBody = "{\"available\":true}".getBytes();
    MockHttpServletRequest request =
        new MockHttpServletRequest(
            "POST",
            "/api/v1/platform/operator/tenants/tenant-default/customers/customer-1/kyc/document-prefill");

    stubForward(
        proxyService,
        "identity",
        "/api/v1/tenants/tenant-default/customers/customer-1/kyc/document-prefill",
        null,
        HttpMethod.POST,
        requestBody,
        responseBody);

    ResponseEntity<byte[]> response =
        controller.prefillKycDocument("tenant-default", "customer-1", request, requestBody);

    assertForwarded(
        response,
        responseBody,
        proxyService,
        "identity",
        "/api/v1/tenants/tenant-default/customers/customer-1/kyc/document-prefill",
        null,
        HttpMethod.POST,
        requestBody);
  }
}
