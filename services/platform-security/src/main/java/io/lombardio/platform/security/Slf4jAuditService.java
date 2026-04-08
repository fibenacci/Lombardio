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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Standard implementation that writes audit events to logs. In a production environment, logs
 * should be collected by a log management system (e.g., Loki, ELK).
 */
public class Slf4jAuditService implements AuditService {

  private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

  @Override
  @SuppressFBWarnings(
      value = "CRLF_INJECTION_LOGS",
      justification = "Audit logs are structured and validated")
  public void record(AuditEvent event) {
    // Structured log format for easy parsing (JSON-like or key-value)
    auditLogger.info(
        "AUDIT | [{}] | Actor: {} | Tenant: {} | Action: {} | Target: {}:{} | Status: {} | TraceId: {} | Metadata: {}",
        event.timestamp(),
        event.actorUserId(),
        event.tenantId(),
        event.action(),
        event.targetType(),
        event.targetId(),
        event.status(),
        event.traceId(),
        event.metadata());
  }
}
