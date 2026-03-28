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

import io.lombardio.platform.security.ForbiddenException;
import io.lombardio.platform.security.UnauthorizedException;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
    List<ApiFieldError> fieldErrors =
        exception.getBindingResult().getFieldErrors().stream().map(this::mapFieldError).toList();

    return ResponseEntity.badRequest()
        .body(new ApiError("validation_error", "Validation failed", fieldErrors));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiError> handleConstraintViolation(
      ConstraintViolationException exception) {
    return ResponseEntity.badRequest()
        .body(new ApiError("validation_error", exception.getMessage(), List.of()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException exception) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ApiError("bad_request", exception.getMessage(), List.of()));
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ApiError> handleUnauthorized(UnauthorizedException exception) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(new ApiError("unauthorized", exception.getMessage(), List.of()));
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ApiError> handleForbidden(ForbiddenException exception) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(new ApiError("forbidden", exception.getMessage(), List.of()));
  }

  private ApiFieldError mapFieldError(FieldError error) {
    return new ApiFieldError(error.getField(), error.getDefaultMessage());
  }
}
