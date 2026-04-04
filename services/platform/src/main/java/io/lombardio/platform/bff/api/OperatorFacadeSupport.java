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
import java.util.Collections;
import java.util.Enumeration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

abstract class OperatorFacadeSupport {

  private final OperatorBffProxyService proxyService;

  protected OperatorFacadeSupport(OperatorBffProxyService proxyService) {
    this.proxyService = proxyService;
  }

  protected ResponseEntity<byte[]> forwardGet(
      String serviceKey, HttpServletRequest request, String downstreamPath) {
    return forward(
        serviceKey, request, HttpMethod.GET, downstreamPath, request.getQueryString(), null);
  }

  protected ResponseEntity<byte[]> forwardGet(
      String serviceKey, HttpServletRequest request, String downstreamPath, String query) {
    return forward(serviceKey, request, HttpMethod.GET, downstreamPath, query, null);
  }

  protected ResponseEntity<byte[]> forwardPost(
      String serviceKey, HttpServletRequest request, String downstreamPath, byte[] body) {
    return forward(
        serviceKey, request, HttpMethod.POST, downstreamPath, request.getQueryString(), body);
  }

  protected ResponseEntity<byte[]> forwardPost(
      String serviceKey,
      HttpServletRequest request,
      String downstreamPath,
      String query,
      byte[] body) {
    return forward(serviceKey, request, HttpMethod.POST, downstreamPath, query, body);
  }

  protected ResponseEntity<byte[]> forwardPut(
      String serviceKey, HttpServletRequest request, String downstreamPath, byte[] body) {
    return forward(
        serviceKey, request, HttpMethod.PUT, downstreamPath, request.getQueryString(), body);
  }

  private ResponseEntity<byte[]> forward(
      String serviceKey,
      HttpServletRequest request,
      HttpMethod method,
      String downstreamPath,
      String query,
      byte[] body) {
    return proxyService.forward(
        serviceKey, downstreamPath, query, method, body, copyHeaders(request));
  }

  protected HttpHeaders copyHeaders(HttpServletRequest request) {
    HttpHeaders headers = new HttpHeaders();
    Enumeration<String> headerNames = request.getHeaderNames();
    if (headerNames == null) {
      return headers;
    }

    for (String headerName : Collections.list(headerNames)) {
      for (String headerValue : Collections.list(request.getHeaders(headerName))) {
        headers.add(headerName, headerValue);
      }
    }

    return headers;
  }
}
