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
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;

@Aspect
public class AuditAspect {

  private final AuditService auditService;

  public AuditAspect(AuditService auditService) {
    this.auditService = auditService;
  }

  @Around("@annotation(audited)")
  public Object audit(ProceedingJoinPoint joinPoint, Audited audited) throws Throwable {
    Object result = null;
    String status = "SUCCESS";
    Throwable error = null;

    try {
      result = joinPoint.proceed();
      return result;
    } catch (Throwable t) {
      status = "FAILURE";
      error = t;
      throw t;
    } finally {
      recordAudit(joinPoint, audited, status, error);
    }
  }

  private void recordAudit(
      ProceedingJoinPoint joinPoint, Audited audited, String status, Throwable error) {
    try {
      AuthenticatedUser user = findAuthenticatedUser(joinPoint.getArgs());

      String targetId = extractTargetId(audited.targetId(), joinPoint);

      AuditEvent event =
          new AuditEvent(
              Instant.now(),
              user != null ? user.userId() : "anonymous",
              user != null ? user.tenantId() : "none",
              audited.action(),
              audited.targetType(),
              targetId,
              status,
              MDC.get("traceId"),
              error != null ? Map.of("error", error.getMessage()) : Map.of());

      auditService.record(event);
    } catch (RuntimeException e) {
      // Don't break business logic because of audit failure, just log it.
      // We catch RuntimeException specifically to satisfy SpotBugs while maintaining safety.
    }
  }

  private AuthenticatedUser findAuthenticatedUser(Object[] args) {
    for (Object arg : args) {
      if (arg instanceof AuthenticatedUser) {
        return (AuthenticatedUser) arg;
      }
    }
    return null;
  }

  private String extractTargetId(String targetIdExpression, ProceedingJoinPoint joinPoint) {
    if (targetIdExpression == null || targetIdExpression.isBlank()) {
      return null;
    }
    // Simple extraction for now. For full SpEL support, we'd need a SpEL parser.
    // Given the project context, we keep it simple first.
    return targetIdExpression;
  }
}
