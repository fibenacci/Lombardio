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
@RequestMapping("/api/v1/platform/operator/tenants/{tenantId}/customers")
public class OperatorCustomerFacadeController extends OperatorFacadeSupport {
  public OperatorCustomerFacadeController(OperatorBffProxyService proxyService) {
    super(proxyService);
  }

  @GetMapping
  public ResponseEntity<byte[]> searchCustomers(
      @PathVariable String tenantId, HttpServletRequest request) {
    return forwardGet("identity", request, "/api/v1/tenants/" + tenantId + "/customers");
  }

  @PostMapping
  public ResponseEntity<byte[]> createCustomer(
      @PathVariable String tenantId,
      HttpServletRequest request,
      @RequestBody(required = false) byte[] body) {
    return forwardPost("identity", request, "/api/v1/tenants/" + tenantId + "/customers", body);
  }

  @GetMapping("/{customerId}")
  public ResponseEntity<byte[]> getCustomer(
      @PathVariable String tenantId, @PathVariable String customerId, HttpServletRequest request) {
    return forwardGet(
        "identity", request, "/api/v1/tenants/" + tenantId + "/customers/" + customerId);
  }

  @PutMapping("/{customerId}")
  public ResponseEntity<byte[]> updateCustomer(
      @PathVariable String tenantId,
      @PathVariable String customerId,
      HttpServletRequest request,
      @RequestBody(required = false) byte[] body) {
    return forwardPut(
        "identity", request, "/api/v1/tenants/" + tenantId + "/customers/" + customerId, body);
  }

  @GetMapping("/{customerId}/kyc")
  public ResponseEntity<byte[]> getKycStatus(
      @PathVariable String tenantId, @PathVariable String customerId, HttpServletRequest request) {
    return forwardGet(
        "identity", request, "/api/v1/tenants/" + tenantId + "/customers/" + customerId + "/kyc");
  }

  @GetMapping("/{customerId}/kyc/documents")
  public ResponseEntity<byte[]> getKycDocuments(
      @PathVariable String tenantId, @PathVariable String customerId, HttpServletRequest request) {
    return forwardGet(
        "identity",
        request,
        "/api/v1/tenants/" + tenantId + "/customers/" + customerId + "/kyc/documents");
  }

  @PostMapping("/{customerId}/kyc")
  public ResponseEntity<byte[]> updateKycStatus(
      @PathVariable String tenantId,
      @PathVariable String customerId,
      HttpServletRequest request,
      @RequestBody(required = false) byte[] body) {
    return forwardPost(
        "identity",
        request,
        "/api/v1/tenants/" + tenantId + "/customers/" + customerId + "/kyc",
        body);
  }

  @PostMapping("/{customerId}/kyc/document-prefill")
  public ResponseEntity<byte[]> prefillKycDocument(
      @PathVariable String tenantId,
      @PathVariable String customerId,
      HttpServletRequest request,
      @RequestBody(required = false) byte[] body) {
    return forwardPost(
        "identity",
        request,
        "/api/v1/tenants/" + tenantId + "/customers/" + customerId + "/kyc/document-prefill",
        body);
  }

  @GetMapping("/{customerId}/aml")
  public ResponseEntity<byte[]> getAmlStatus(
      @PathVariable String tenantId, @PathVariable String customerId, HttpServletRequest request) {
    return forwardGet(
        "identity", request, "/api/v1/tenants/" + tenantId + "/customers/" + customerId + "/aml");
  }

  @PostMapping("/{customerId}/aml")
  public ResponseEntity<byte[]> updateAmlStatus(
      @PathVariable String tenantId,
      @PathVariable String customerId,
      HttpServletRequest request,
      @RequestBody(required = false) byte[] body) {
    return forwardPost(
        "identity",
        request,
        "/api/v1/tenants/" + tenantId + "/customers/" + customerId + "/aml",
        body);
  }

  @PostMapping("/{customerId}/aml/origination-check")
  public ResponseEntity<byte[]> assessAmlOrigination(
      @PathVariable String tenantId,
      @PathVariable String customerId,
      HttpServletRequest request,
      @RequestBody(required = false) byte[] body) {
    return forwardPost(
        "identity",
        request,
        "/api/v1/tenants/" + tenantId + "/customers/" + customerId + "/aml/origination-check",
        body);
  }
}
