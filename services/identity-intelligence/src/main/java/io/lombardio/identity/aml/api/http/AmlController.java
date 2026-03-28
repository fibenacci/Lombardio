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
package io.lombardio.identity.aml.api.http;

import io.lombardio.identity.aml.application.service.AmlService;
import io.lombardio.identity.aml.infrastructure.security.AmlAuthorizationService;
import io.lombardio.platform.security.AuthenticatedUser;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/customers/{customerId}/aml")
public class AmlController {

  private final AmlService amlService;
  private final AmlAuthorizationService authorizationService;

  public AmlController(AmlService amlService, AmlAuthorizationService authorizationService) {
    this.amlService = amlService;
    this.authorizationService = authorizationService;
  }

  @GetMapping
  public AmlStatusResponse getStatus(
      @AuthenticationPrincipal AuthenticatedUser principal,
      @PathVariable String tenantId,
      @PathVariable String customerId) {
    authorizationService.requireRead(principal, tenantId);
    return amlService.getStatus(tenantId, customerId);
  }

  @PostMapping
  public AmlStatusResponse updateStatus(
      @AuthenticationPrincipal AuthenticatedUser principal,
      @PathVariable String tenantId,
      @PathVariable String customerId,
      @Valid @RequestBody UpdateAmlStatusRequest request) {
    authorizationService.requireWrite(principal, tenantId);
    return amlService.updateStatus(tenantId, customerId, request);
  }

  @PostMapping("/origination-check")
  public AmlStatusResponse assessForOrigination(
      @AuthenticationPrincipal AuthenticatedUser principal,
      @PathVariable String tenantId,
      @PathVariable String customerId,
      @Valid @RequestBody OriginationAssessmentRequest request) {
    authorizationService.requireRead(principal, tenantId);
    return amlService.assessForOrigination(tenantId, customerId, request);
  }

  @GetMapping("/health")
  public Map<String, String> health() {
    return Map.of("status", "ok");
  }
}
