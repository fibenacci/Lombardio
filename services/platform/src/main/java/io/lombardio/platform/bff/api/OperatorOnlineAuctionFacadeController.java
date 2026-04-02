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

import io.lombardio.platform.bff.application.OperatorBffProxyService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/platform/operator/tenants/{tenantId}/online-auctions")
public class OperatorOnlineAuctionFacadeController extends OperatorFacadeSupport {
  public OperatorOnlineAuctionFacadeController(OperatorBffProxyService proxyService) {
    super(proxyService);
  }

  @GetMapping
  public ResponseEntity<byte[]> list(@PathVariable String tenantId, HttpServletRequest request) {
    return forwardGet("online-auction", request, "/api/v1/tenants/" + tenantId + "/online-auctions");
  }

  @PostMapping
  public ResponseEntity<byte[]> create(
      @PathVariable String tenantId,
      HttpServletRequest request,
      @RequestBody(required = false) byte[] body) {
    return forwardPost("online-auction", request, "/api/v1/tenants/" + tenantId + "/online-auctions", null, body);
  }

  @PostMapping("/{auctionId}/publish")
  public ResponseEntity<byte[]> publish(
      @PathVariable String tenantId, @PathVariable String auctionId, HttpServletRequest request) {
    return forwardPost(
        "online-auction",
        request,
        "/api/v1/tenants/" + tenantId + "/online-auctions/" + auctionId + "/publish",
        null,
        new byte[0]);
  }

  @PostMapping("/{auctionId}/start")
  public ResponseEntity<byte[]> start(
      @PathVariable String tenantId, @PathVariable String auctionId, HttpServletRequest request) {
    return forwardPost(
        "online-auction",
        request,
        "/api/v1/tenants/" + tenantId + "/online-auctions/" + auctionId + "/start",
        null,
        new byte[0]);
  }

  @PostMapping("/{auctionId}/close")
  public ResponseEntity<byte[]> close(
      @PathVariable String tenantId, @PathVariable String auctionId, HttpServletRequest request) {
    return forwardPost(
        "online-auction",
        request,
        "/api/v1/tenants/" + tenantId + "/online-auctions/" + auctionId + "/close",
        null,
        new byte[0]);
  }

  @PostMapping("/{auctionId}/registrations/{registrationId}/review")
  public ResponseEntity<byte[]> reviewRegistration(
      @PathVariable String tenantId,
      @PathVariable String auctionId,
      @PathVariable String registrationId,
      HttpServletRequest request,
      @RequestBody(required = false) byte[] body) {
    return forwardPost(
        "online-auction",
        request,
        "/api/v1/tenants/" + tenantId + "/online-auctions/" + auctionId + "/registrations/" + registrationId + "/review",
        null,
        body);
  }
}
