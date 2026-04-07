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
package io.lombardio.platform.bff.application;

import io.lombardio.platform.security.AuthenticatedUser;
import io.lombardio.platform.security.UnauthorizedException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Service
public class OperatorBffProxyService {

  private final RestClient restClient;
  private final OperatorBffTargetResolver targetResolver;
  private final OperatorBffHeaderPolicy headerPolicy;

  public OperatorBffProxyService(
      RestClient.Builder restClientBuilder,
      OperatorBffTargetResolver targetResolver,
      OperatorBffHeaderPolicy headerPolicy) {
    this.restClient = restClientBuilder.build();
    this.targetResolver = targetResolver;
    this.headerPolicy = headerPolicy;
  }

  public ResponseEntity<StreamingResponseBody> forward(
      String serviceKey,
      String downstreamPath,
      String query,
      HttpMethod method,
      byte[] body,
      HttpHeaders incomingHeaders) {
    var targetUri = targetResolver.resolve(serviceKey, downstreamPath, query);
    var requestSpec =
        restClient
            .method(method)
            .uri(targetUri)
            .headers(
                headers ->
                    headerPolicy.applyForwardHeaders(
                        headers, incomingHeaders, currentAccessToken()));

    var exchangeSpec =
        body != null && body.length > 0 && method != HttpMethod.GET && method != HttpMethod.DELETE
            ? requestSpec.body(body)
            : requestSpec;

    return exchangeSpec.exchange(
        (request, response) -> {
          HttpHeaders responseHeaders = headerPolicy.sanitizeResponseHeaders(response.getHeaders());
          StreamingResponseBody responseBody =
              outputStream -> StreamUtils.copy(response.getBody(), outputStream);
          return new ResponseEntity<>(
              responseBody,
              responseHeaders,
              HttpStatusCode.valueOf(response.getStatusCode().value()));
        });
  }

  private String currentAccessToken() {
    return AuthenticatedUser.currentAccessToken()
        .orElseThrow(() -> new UnauthorizedException("Missing operator session"));
  }
}
