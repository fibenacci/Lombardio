package io.lombardio.platform.bff.api;

import static org.mockito.Mockito.mock;

import io.lombardio.platform.bff.application.OperatorBffProxyService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

class OperatorOriginationFacadeControllerTest extends OperatorFacadeControllerTestSupport {

  @Test
  void forwardsLoanListToOriginationService() {
    OperatorBffProxyService proxyService = mock(OperatorBffProxyService.class);
    OperatorOriginationFacadeController controller = new OperatorOriginationFacadeController(proxyService);
    MockHttpServletRequest request =
        new MockHttpServletRequest("GET", "/api/v1/platform/operator/tenants/tenant-default/loans");
    request.setQueryString("customerId=customer-1");
    byte[] responseBody = "[]".getBytes();

    stubForward(
        proxyService,
        "origination",
        "/api/v1/tenants/tenant-default/loans",
        "customerId=customer-1",
        HttpMethod.GET,
        null,
        responseBody);

    ResponseEntity<byte[]> response = controller.listLoans("tenant-default", request);

    assertForwarded(
        response,
        responseBody,
        proxyService,
        "origination",
        "/api/v1/tenants/tenant-default/loans",
        "customerId=customer-1",
        HttpMethod.GET,
        null);
  }

  @Test
  void forwardsLoanCreationToOriginationService() {
    OperatorBffProxyService proxyService = mock(OperatorBffProxyService.class);
    OperatorOriginationFacadeController controller = new OperatorOriginationFacadeController(proxyService);
    byte[] requestBody = "{\"customerId\":\"customer-1\"}".getBytes();
    byte[] responseBody = "{\"id\":\"loan-1\"}".getBytes();
    MockHttpServletRequest request =
        new MockHttpServletRequest("POST", "/api/v1/platform/operator/tenants/tenant-default/loans");

    stubForward(
        proxyService,
        "origination",
        "/api/v1/tenants/tenant-default/loans",
        null,
        HttpMethod.POST,
        requestBody,
        responseBody);

    ResponseEntity<byte[]> response = controller.createLoan("tenant-default", request, requestBody);

    assertForwarded(
        response,
        responseBody,
        proxyService,
        "origination",
        "/api/v1/tenants/tenant-default/loans",
        null,
        HttpMethod.POST,
        requestBody);
  }
}
