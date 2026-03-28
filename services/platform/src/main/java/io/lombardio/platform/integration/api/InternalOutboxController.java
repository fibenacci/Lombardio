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
package io.lombardio.platform.integration.api;

import io.lombardio.platform.integration.application.OutboxEventResponse;
import io.lombardio.platform.integration.application.PlatformOutboxService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/internal/v1/outbox-events")
public class InternalOutboxController {

  private final PlatformOutboxService platformOutboxService;
  private final IntegrationOutboxProperties properties;

  public InternalOutboxController(
      PlatformOutboxService platformOutboxService, IntegrationOutboxProperties properties) {
    this.platformOutboxService = platformOutboxService;
    this.properties = properties;
  }

  @PostMapping("/claim")
  List<OutboxEventResponse> claim(
      @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorization,
      @Valid @RequestBody ClaimOutboxEventsRequest request) {
    requireToken(authorization);
    return platformOutboxService.claim(request.consumer(), request.limit());
  }

  @PostMapping("/{id}/complete")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void complete(
      @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorization,
      @PathVariable String id,
      @Valid @RequestBody CompleteOutboxEventRequest request) {
    requireToken(authorization);
    platformOutboxService.complete(id, request.consumer());
  }

  @PostMapping("/{id}/fail")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void fail(
      @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorization,
      @PathVariable String id,
      @Valid @RequestBody FailOutboxEventRequest request) {
    requireToken(authorization);
    platformOutboxService.fail(id, request.consumer(), request.errorMessage());
  }

  private void requireToken(String authorization) {
    String expected = properties.accessToken();
    if (expected == null || expected.isBlank()) {
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Outbox integration token is not configured");
    }

    String actual =
        authorization != null && authorization.startsWith("Bearer ")
            ? authorization.substring(7)
            : null;

    if (!expected.equals(actual)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid integration token");
    }
  }
}
