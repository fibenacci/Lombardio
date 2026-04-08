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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.lombardio.platform.bff.application.OperatorBffProxyService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

abstract class OperatorFacadeControllerTestSupport {

  protected ResponseEntity<StreamingResponseBody> stubForward(
      OperatorBffProxyService proxyService,
      String serviceKey,
      String downstreamPath,
      String query,
      HttpMethod method,
      byte[] requestBody) {
    StreamingResponseBody responseBody = output -> {};
    ResponseEntity<StreamingResponseBody> entity =
        new ResponseEntity<>(responseBody, HttpStatus.OK);
    when(proxyService.forward(
            eq(serviceKey),
            eq(downstreamPath),
            eq(query),
            eq(method),
            eq(requestBody),
            any(HttpHeaders.class)))
        .thenReturn(entity);
    return entity;
  }

  protected void assertForwarded(
      ResponseEntity<StreamingResponseBody> response,
      OperatorBffProxyService proxyService,
      String serviceKey,
      String downstreamPath,
      String query,
      HttpMethod method,
      byte[] requestBody) {
    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(proxyService)
        .forward(
            eq(serviceKey),
            eq(downstreamPath),
            eq(query),
            eq(method),
            eq(requestBody),
            any(HttpHeaders.class));
  }
}
