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
package io.lombardio.platform.security;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.slf4j.MDC;

public final class TraceIdContext {

  public static final String TRACE_ID_ATTRIBUTE = "lombardio.trace_id";
  public static final String TRACE_ID_HEADER = "X-Trace-Id";
  public static final String TRACE_ID_MDC_KEY = "traceId";

  private TraceIdContext() {}

  public static String getOrCreate(HttpServletRequest request) {
    Object existing = request.getAttribute(TRACE_ID_ATTRIBUTE);
    if (existing instanceof String traceId && !traceId.isBlank()) {
      return traceId;
    }

    String generated = UUID.randomUUID().toString();
    request.setAttribute(TRACE_ID_ATTRIBUTE, generated);
    return generated;
  }

  public static String currentOrFallback() {
    String traceId = MDC.get(TRACE_ID_MDC_KEY);
    return traceId != null && !traceId.isBlank() ? traceId : "unknown";
  }
}
