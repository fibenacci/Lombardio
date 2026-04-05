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
package io.lombardio.identity.api.http.error;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.lombardio.platform.security.TraceIdContext;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidation(
      MethodArgumentNotValidException exception, HttpServletRequest request) {
    List<ApiFieldError> errors =
        exception.getBindingResult().getFieldErrors().stream().map(this::mapFieldError).toList();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            new ApiError(
                "validation_failed",
                "Input validation failed",
                TraceIdContext.getOrCreate(request),
                errors));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiError> handleIllegal(
      IllegalArgumentException exception, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            new ApiError(
                "bad_request",
                exception.getMessage(),
                TraceIdContext.getOrCreate(request),
                List.of()));
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiError> handleForbidden(
      AccessDeniedException exception, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(
            new ApiError(
                "forbidden",
                exception.getMessage(),
                TraceIdContext.getOrCreate(request),
                List.of()));
  }

  @ExceptionHandler(Throwable.class)
  @SuppressFBWarnings(
      value = "CRLF_INJECTION_LOGS",
      justification = "traceId is sanitized via whitelist regex")
  public ResponseEntity<ApiError> handleUnexpected(
      Throwable exception, HttpServletRequest request) {
    String traceId = TraceIdContext.getOrCreate(request);
    String sanitizedTraceId = traceId == null ? "null" : traceId.replaceAll("[^a-zA-Z0-9-]", "_");
    log.error("Unhandled identity service error [traceId={}]", sanitizedTraceId, exception);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ApiError("internal_error", "Internal server error", traceId, List.of()));
  }

  private ApiFieldError mapFieldError(FieldError error) {
    return new ApiFieldError(error.getField(), error.getDefaultMessage());
  }
}
