package io.lombardio.platform.bff.api;

import static org.mockito.Mockito.mock;

import io.lombardio.platform.bff.application.OperatorBffProxyService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

class OperatorOnlineAuctionFacadeControllerTest extends OperatorFacadeControllerTestSupport {

  @Test
  void forwardsListToOnlineAuctionService() {
    OperatorBffProxyService proxyService = mock(OperatorBffProxyService.class);
    OperatorOnlineAuctionFacadeController controller =
        new OperatorOnlineAuctionFacadeController(proxyService);
    MockHttpServletRequest request =
        new MockHttpServletRequest(
            "GET", "/api/v1/platform/operator/tenants/tenant-default/online-auctions");
    byte[] responseBody = "[]".getBytes();

    stubForward(
        proxyService,
        "online-auction",
        "/api/v1/tenants/tenant-default/online-auctions",
        null,
        HttpMethod.GET,
        null,
        responseBody);

    ResponseEntity<byte[]> response = controller.list("tenant-default", request);

    assertForwarded(
        response,
        responseBody,
        proxyService,
        "online-auction",
        "/api/v1/tenants/tenant-default/online-auctions",
        null,
        HttpMethod.GET,
        null);
  }

  @Test
  void forwardsRegistrationReviewToOnlineAuctionService() {
    OperatorBffProxyService proxyService = mock(OperatorBffProxyService.class);
    OperatorOnlineAuctionFacadeController controller =
        new OperatorOnlineAuctionFacadeController(proxyService);
    byte[] requestBody = "{\"decision\":\"APPROVE\"}".getBytes();
    byte[] responseBody = "{\"id\":\"auction-1\"}".getBytes();
    MockHttpServletRequest request =
        new MockHttpServletRequest(
            "POST",
            "/api/v1/platform/operator/tenants/tenant-default/online-auctions/auction-1/registrations/reg-1/review");

    stubForward(
        proxyService,
        "online-auction",
        "/api/v1/tenants/tenant-default/online-auctions/auction-1/registrations/reg-1/review",
        null,
        HttpMethod.POST,
        requestBody,
        responseBody);

    ResponseEntity<byte[]> response =
        controller.reviewRegistration("tenant-default", "auction-1", "reg-1", request, requestBody);

    assertForwarded(
        response,
        responseBody,
        proxyService,
        "online-auction",
        "/api/v1/tenants/tenant-default/online-auctions/auction-1/registrations/reg-1/review",
        null,
        HttpMethod.POST,
        requestBody);
  }
}
