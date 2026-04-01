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
package io.lombardio.identity.kyc.api;

import io.lombardio.identity.kyc.application.KycService;
import io.lombardio.identity.kyc.infrastructure.security.KycAuthorizationService;
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
@RequestMapping("/api/v1/tenants/{tenantId}/customers/{customerId}/kyc")
public class KycController {

  private final KycService kycService;
  private final KycAuthorizationService authorizationService;

  public KycController(KycService kycService, KycAuthorizationService authorizationService) {
    this.kycService = kycService;
    this.authorizationService = authorizationService;
  }

  @GetMapping
  public KycStatusResponse getStatus(
      @AuthenticationPrincipal AuthenticatedUser principal,
      @PathVariable String tenantId,
      @PathVariable String customerId) {
    authorizationService.requireRead(principal, tenantId);
    return kycService.getStatus(tenantId, customerId);
  }

  @GetMapping("/documents")
  public KycDocumentImagesResponse getDocuments(
      @AuthenticationPrincipal AuthenticatedUser principal,
      @PathVariable String tenantId,
      @PathVariable String customerId) {
    authorizationService.requireDocumentRead(principal, tenantId);
    return kycService.getDocumentImages(tenantId, customerId);
  }

  @PostMapping
  public KycStatusResponse updateStatus(
      @AuthenticationPrincipal AuthenticatedUser principal,
      @PathVariable String tenantId,
      @PathVariable String customerId,
      @Valid @RequestBody UpdateKycStatusRequest request) {
    authorizationService.requireWrite(principal, tenantId);
    return kycService.updateStatus(tenantId, customerId, request);
  }

  @PostMapping("/document-prefill")
  public DocumentPrefillResponse prefillDocument(
      @AuthenticationPrincipal AuthenticatedUser principal,
      @PathVariable String tenantId,
      @PathVariable String customerId,
      @Valid @RequestBody DocumentPrefillRequest request) {
    authorizationService.requireWrite(principal, tenantId);
    return kycService.prefillDocumentData(tenantId, request);
  }

  @GetMapping("/approval")
  public Map<String, Boolean> approval(
      @AuthenticationPrincipal AuthenticatedUser principal,
      @PathVariable String tenantId,
      @PathVariable String customerId) {
    authorizationService.requireRead(principal, tenantId);
    return Map.of("approved", kycService.isApproved(tenantId, customerId));
  }
}
