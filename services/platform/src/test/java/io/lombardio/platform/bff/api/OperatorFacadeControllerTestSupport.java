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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
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

abstract class OperatorFacadeControllerTestSupport {

  protected ResponseEntity<byte[]> stubForward(
      OperatorBffProxyService proxyService,
      String serviceKey,
      String downstreamPath,
      String query,
      HttpMethod method,
      byte[] requestBody,
      byte[] responseBody) {
    when(proxyService.forward(
            eq(serviceKey),
            eq(downstreamPath),
            eq(query),
            eq(method),
            eq(requestBody),
            any(HttpHeaders.class)))
        .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));
    return new ResponseEntity<>(responseBody, HttpStatus.OK);
  }

  protected void assertForwarded(
      ResponseEntity<byte[]> response,
      byte[] responseBody,
      OperatorBffProxyService proxyService,
      String serviceKey,
      String downstreamPath,
      String query,
      HttpMethod method,
      byte[] requestBody) {
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertArrayEquals(responseBody, response.getBody());
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
