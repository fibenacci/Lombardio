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
@RequestMapping("/api/v1/platform/operator/tenants/{tenantId}/reporting")
public class OperatorReportingFacadeController extends OperatorFacadeSupport {
  public OperatorReportingFacadeController(OperatorBffProxyService proxyService) {
    super(proxyService);
  }

  @GetMapping("/dashboard")
  public ResponseEntity<byte[]> dashboard(
      @PathVariable String tenantId, HttpServletRequest request) {
    return forwardGet("reporting", request, "/api/v1/tenants/" + tenantId + "/reporting/dashboard");
  }
}
