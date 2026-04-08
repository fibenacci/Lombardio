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
import io.lombardio.platform.security.AuditEvent;
import io.lombardio.platform.security.AuditService;
import io.lombardio.platform.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api/v1/platform/operator/tenants/{tenantId}")
public class GenericOperatorFacadeController extends OperatorFacadeSupport {

  private final OperatorBffAuthorizationService authorizationService;
  private final AuditService auditService;

  public GenericOperatorFacadeController(
      OperatorBffProxyService proxyService,
      OperatorBffAuthorizationService authorizationService,
      AuditService auditService) {
    super(proxyService);
    this.authorizationService = authorizationService;
    this.auditService = auditService;
  }

  @GetMapping("/{serviceKey}/**")
  public ResponseEntity<StreamingResponseBody> forwardGet(
      @AuthenticationPrincipal AuthenticatedUser principal,
      @PathVariable String tenantId,
      @PathVariable String serviceKey,
      HttpServletRequest request) {
    authorizationService.requireTenantAccess(principal, tenantId, serviceKey);
    auditForward(principal, tenantId, serviceKey, HttpMethod.GET);
    return forwardRequest(tenantId, serviceKey, request, null, HttpMethod.GET);
  }

  @PostMapping("/{serviceKey}/**")
  public ResponseEntity<StreamingResponseBody> forwardPost(
      @AuthenticationPrincipal AuthenticatedUser principal,
      @PathVariable String tenantId,
      @PathVariable String serviceKey,
      HttpServletRequest request,
      @RequestBody(required = false) byte[] body) {
    authorizationService.requireTenantAccess(principal, tenantId, serviceKey);
    auditForward(principal, tenantId, serviceKey, HttpMethod.POST);
    return forwardRequest(tenantId, serviceKey, request, body, HttpMethod.POST);
  }

  @PutMapping("/{serviceKey}/**")
  public ResponseEntity<StreamingResponseBody> forwardPut(
      @AuthenticationPrincipal AuthenticatedUser principal,
      @PathVariable String tenantId,
      @PathVariable String serviceKey,
      HttpServletRequest request,
      @RequestBody(required = false) byte[] body) {
    authorizationService.requireTenantAccess(principal, tenantId, serviceKey);
    auditForward(principal, tenantId, serviceKey, HttpMethod.PUT);
    return forwardRequest(tenantId, serviceKey, request, body, HttpMethod.PUT);
  }

  @DeleteMapping("/{serviceKey}/**")
  public ResponseEntity<StreamingResponseBody> forwardDelete(
      @AuthenticationPrincipal AuthenticatedUser principal,
      @PathVariable String tenantId,
      @PathVariable String serviceKey,
      HttpServletRequest request) {
    authorizationService.requireTenantAccess(principal, tenantId, serviceKey);
    auditForward(principal, tenantId, serviceKey, HttpMethod.DELETE);
    return forwardRequest(tenantId, serviceKey, request, null, HttpMethod.DELETE);
  }

  private void auditForward(
      AuthenticatedUser principal, String tenantId, String serviceKey, HttpMethod method) {
    auditService.record(
        AuditEvent.create(
            principal, "FORWARD_REQUEST", "service:" + serviceKey, tenantId, "SUCCESS", null));
  }

  private ResponseEntity<StreamingResponseBody> forwardRequest(
      String tenantId,
      String serviceKey,
      HttpServletRequest request,
      byte[] body,
      HttpMethod method) {

    String fullPath =
        (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
    String bestMatchPattern =
        (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
    String remainingPath =
        new AntPathMatcher().extractPathWithinPattern(bestMatchPattern, fullPath);

    if (remainingPath.contains("..") || remainingPath.contains("//")) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid path sequence in request");
    }

    // Standardize mapping: serviceKey -> actual downstream service path
    // Most services follow /api/v1/tenants/{tenantId}/...
    String downstreamPath =
        "/api/v1/tenants/"
            + tenantId
            + "/"
            + (remainingPath.isEmpty() ? serviceKey : serviceKey + "/" + remainingPath);

    return proxyService.forward(
        serviceKey, downstreamPath, request.getQueryString(), method, body, copyHeaders(request));
  }
}
