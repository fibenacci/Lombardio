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

import java.time.Instant;
import java.util.Map;

/**
 * Represents an immutable audit trail entry for security- and business-relevant actions.
 */
public record AuditEvent(
    Instant timestamp,
    String actorUserId,
    String tenantId,
    String action,
    String targetType,
    String targetId,
    String traceId,
    String status, // e.g., SUCCESS, FAILURE
    Map<String, String> metadata) {

  public AuditEvent {
    metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
  }

  @Override
  public Map<String, String> metadata() {
    return Map.copyOf(metadata);
  }

  public static AuditEvent create(
      AuthenticatedUser actor,
      String action,
      String targetType,
      String targetId,
      String status,
      Map<String, String> metadata) {
    return new AuditEvent(
        Instant.now(),
        actor != null ? actor.actorUserId() : "system",
        actor != null ? actor.tenantId() : "system",
        action,
        targetType,
        targetId,
        TraceIdContext.currentOrFallback(),
        status,
        metadata);
  }
}
