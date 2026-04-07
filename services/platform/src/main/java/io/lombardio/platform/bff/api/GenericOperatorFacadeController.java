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
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

@RestController
@RequestMapping("/api/v1/platform/operator/tenants/{tenantId}")
public class GenericOperatorFacadeController extends OperatorFacadeSupport {

  public GenericOperatorFacadeController(OperatorBffProxyService proxyService) {
    super(proxyService);
  }

  @GetMapping("/{serviceKey}/**")
  public ResponseEntity<byte[]> forwardGet(
      @PathVariable String tenantId, @PathVariable String serviceKey, HttpServletRequest request) {
    return forwardRequest(tenantId, serviceKey, request, null, HttpMethod.GET);
  }

  @PostMapping("/{serviceKey}/**")
  public ResponseEntity<byte[]> forwardPost(
      @PathVariable String tenantId,
      @PathVariable String serviceKey,
      HttpServletRequest request,
      @RequestBody(required = false) byte[] body) {
    return forwardRequest(tenantId, serviceKey, request, body, HttpMethod.POST);
  }

  @PutMapping("/{serviceKey}/**")
  public ResponseEntity<byte[]> forwardPut(
      @PathVariable String tenantId,
      @PathVariable String serviceKey,
      HttpServletRequest request,
      @RequestBody(required = false) byte[] body) {
    return forwardRequest(tenantId, serviceKey, request, body, HttpMethod.PUT);
  }

  @DeleteMapping("/{serviceKey}/**")
  public ResponseEntity<byte[]> forwardDelete(
      @PathVariable String tenantId, @PathVariable String serviceKey, HttpServletRequest request) {
    return forwardRequest(tenantId, serviceKey, request, null, HttpMethod.DELETE);
  }

  private ResponseEntity<byte[]> forwardRequest(
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

    // Standardize mapping: serviceKey -> actual downstream service path
    // Most services follow /api/v1/tenants/{tenantId}/...
    String downstreamPath =
        "/api/v1/tenants/"
            + tenantId
            + "/"
            + serviceKey
            + (remainingPath.isEmpty() ? "" : "/" + remainingPath);

    return proxyService.forward(
        serviceKey, downstreamPath, request.getQueryString(), method, body, copyHeaders(request));
  }
}
