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
@RequestMapping("/api/v1/platform/operator/tenants/{tenantId}")
public class OperatorAuctionFacadeController extends OperatorFacadeSupport {
  public OperatorAuctionFacadeController(OperatorBffProxyService proxyService) {
    super(proxyService);
  }

  @GetMapping("/auctions")
  public ResponseEntity<byte[]> listAuctions(
      @PathVariable String tenantId, HttpServletRequest request) {
    return forwardGet("auction", request, "/api/v1/tenants/" + tenantId + "/auctions", null);
  }

  @PostMapping("/auctions")
  public ResponseEntity<byte[]> createAuction(
      @PathVariable String tenantId,
      HttpServletRequest request,
      @RequestBody(required = false) byte[] body) {
    return forwardPost("auction", request, "/api/v1/tenants/" + tenantId + "/auctions", null, body);
  }

  @PostMapping("/auctions/{auctionId}/announce")
  public ResponseEntity<byte[]> announceAuction(
      @PathVariable String tenantId,
      @PathVariable String auctionId,
      HttpServletRequest request,
      @RequestBody(required = false) byte[] body) {
    return forwardPost(
        "auction",
        request,
        "/api/v1/tenants/" + tenantId + "/auctions/" + auctionId + "/announce",
        null,
        body);
  }

  @PostMapping("/auctions/{auctionId}/open")
  public ResponseEntity<byte[]> openAuction(
      @PathVariable String tenantId, @PathVariable String auctionId, HttpServletRequest request) {
    return forwardPost(
        "auction",
        request,
        "/api/v1/tenants/" + tenantId + "/auctions/" + auctionId + "/open",
        null,
        new byte[0]);
  }

  @PostMapping("/auctions/{auctionId}/close")
  public ResponseEntity<byte[]> closeAuction(
      @PathVariable String tenantId, @PathVariable String auctionId, HttpServletRequest request) {
    return forwardPost(
        "auction",
        request,
        "/api/v1/tenants/" + tenantId + "/auctions/" + auctionId + "/close",
        null,
        new byte[0]);
  }

  @PostMapping("/auctions/{auctionId}/lots/{lotId}/bids")
  public ResponseEntity<byte[]> placeBid(
      @PathVariable String tenantId,
      @PathVariable String auctionId,
      @PathVariable String lotId,
      HttpServletRequest request,
      @RequestBody(required = false) byte[] body) {
    return forwardPost(
        "auction",
        request,
        "/api/v1/tenants/" + tenantId + "/auctions/" + auctionId + "/lots/" + lotId + "/bids",
        null,
        body);
  }

  @PostMapping("/auctions/{auctionId}/lots/{lotId}/settle")
  public ResponseEntity<byte[]> settleLot(
      @PathVariable String tenantId,
      @PathVariable String auctionId,
      @PathVariable String lotId,
      HttpServletRequest request,
      @RequestBody(required = false) byte[] body) {
    return forwardPost(
        "auction",
        request,
        "/api/v1/tenants/" + tenantId + "/auctions/" + auctionId + "/lots/" + lotId + "/settle",
        null,
        body);
  }

  @GetMapping("/surplus-cases")
  public ResponseEntity<byte[]> listSurplusCases(
      @PathVariable String tenantId, HttpServletRequest request) {
    return forwardGet("auction", request, "/api/v1/tenants/" + tenantId + "/surplus-cases");
  }
}
