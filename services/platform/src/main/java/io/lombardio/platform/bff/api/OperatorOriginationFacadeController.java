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
public class OperatorOriginationFacadeController extends OperatorFacadeSupport {
  public OperatorOriginationFacadeController(OperatorBffProxyService proxyService) {
    super(proxyService);
  }

  @GetMapping("/valuation-guidelines")
  public ResponseEntity<byte[]> listValuationGuidelines(
      @PathVariable String tenantId, HttpServletRequest request) {
    return forwardGet(
        "origination", request, "/api/v1/tenants/" + tenantId + "/valuation-guidelines");
  }

  @GetMapping("/loans")
  public ResponseEntity<byte[]> listLoans(
      @PathVariable String tenantId, HttpServletRequest request) {
    return forwardGet("origination", request, "/api/v1/tenants/" + tenantId + "/loans");
  }

  @PostMapping("/loans")
  public ResponseEntity<byte[]> createLoan(
      @PathVariable String tenantId,
      HttpServletRequest request,
      @RequestBody(required = false) byte[] body) {
    return forwardPost("origination", request, "/api/v1/tenants/" + tenantId + "/loans", body);
  }
}
