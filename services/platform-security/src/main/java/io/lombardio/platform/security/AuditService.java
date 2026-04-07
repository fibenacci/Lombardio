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

/**
 * Port for audit event recording.
 * Concrete adapters can implement this via database, event-stream, or logs.
 */
public interface AuditService {
  void record(AuditEvent event);
}
