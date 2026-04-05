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
package io.lombardio.auction.api.http.error;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.lombardio.auction.application.service.AuctionNotFoundException;
import io.lombardio.platform.security.ForbiddenException;
import io.lombardio.platform.security.TraceIdContext;
import io.lombardio.platform.security.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

  @ExceptionHandler(IllegalArgumentException.class)
  ResponseEntity<ApiError> handleIllegalArgument(
      IllegalArgumentException exception, HttpServletRequest request) {
    return ResponseEntity.badRequest()
        .body(
            new ApiError(
                "validation_error",
                exception.getMessage(),
                TraceIdContext.getOrCreate(request),
                List.of()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiError> handleValidation(
      MethodArgumentNotValidException exception, HttpServletRequest request) {
    List<ApiFieldError> fieldErrors =
        exception.getBindingResult().getFieldErrors().stream().map(this::toFieldError).toList();
    return ResponseEntity.badRequest()
        .body(
            new ApiError(
                "validation_error",
                "Validation failed",
                TraceIdContext.getOrCreate(request),
                fieldErrors));
  }

  @ExceptionHandler(AuctionNotFoundException.class)
  ResponseEntity<ApiError> handleNotFound(
      AuctionNotFoundException exception, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(
            new ApiError(
                "not_found",
                exception.getMessage(),
                TraceIdContext.getOrCreate(request),
                List.of()));
  }

  @ExceptionHandler(UnauthorizedException.class)
  ResponseEntity<ApiError> handleUnauthorized(
      UnauthorizedException exception, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(
            new ApiError(
                "unauthorized",
                exception.getMessage(),
                TraceIdContext.getOrCreate(request),
                List.of()));
  }

  @ExceptionHandler(ForbiddenException.class)
  ResponseEntity<ApiError> handleForbidden(
      ForbiddenException exception, HttpServletRequest request) {
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
  ResponseEntity<ApiError> handleUnexpected(Throwable exception, HttpServletRequest request) {
    String traceId = TraceIdContext.getOrCreate(request);
    String sanitizedTraceId = traceId == null ? "null" : traceId.replaceAll("[^a-zA-Z0-9-]", "_");
    log.error("Unhandled auction service error [traceId={}]", sanitizedTraceId, exception);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ApiError("internal_error", "Internal server error", traceId, List.of()));
  }

  private ApiFieldError toFieldError(FieldError fieldError) {
    return new ApiFieldError(fieldError.getField(), fieldError.getDefaultMessage());
  }
}
