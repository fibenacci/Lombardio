package io.lombardio.platform.bff.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

class OperatorBffHeaderPolicyTest {

  @Test
  void filtersSensitiveForwardHeadersAndSetsBearerToken() {
    OperatorBffHeaderPolicy policy = new OperatorBffHeaderPolicy();
    HttpHeaders incoming = new HttpHeaders();
    incoming.add(HttpHeaders.AUTHORIZATION, "Bearer ignored");
    incoming.add(HttpHeaders.COOKIE, "session=abc");
    incoming.add("X-Trace-Id", "trace-1");
    HttpHeaders target = new HttpHeaders();

    policy.applyForwardHeaders(target, incoming, "access-token");

    assertFalse(target.containsKey(HttpHeaders.COOKIE));
    assertEquals("Bearer access-token", target.getFirst(HttpHeaders.AUTHORIZATION));
    assertEquals("trace-1", target.getFirst("X-Trace-Id"));
  }

  @Test
  void removesHopByHopResponseHeadersAndAppliesFallbackContentType() {
    OperatorBffHeaderPolicy policy = new OperatorBffHeaderPolicy();
    HttpHeaders source = new HttpHeaders();
    source.add(HttpHeaders.SET_COOKIE, "s=1");
    source.add("X-Trace-Id", "trace-1");

    HttpHeaders sanitized = policy.sanitizeResponseHeaders(source);

    assertFalse(sanitized.containsKey(HttpHeaders.SET_COOKIE));
    assertEquals("trace-1", sanitized.getFirst("X-Trace-Id"));
    assertEquals("application/octet-stream", sanitized.getContentType().toString());
  }
}
