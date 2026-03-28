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
package io.lombardio.onlineauction.api;

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
  ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
    List<ApiFieldError> fieldErrors =
        exception.getBindingResult().getFieldErrors().stream().map(this::toFieldError).toList();
    return ResponseEntity.badRequest()
        .body(new ApiError("validation_error", "Validation failed", fieldErrors));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  ResponseEntity<ApiError> handleConstraint(ConstraintViolationException exception) {
    return ResponseEntity.badRequest()
        .body(new ApiError("validation_error", exception.getMessage(), List.of()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException exception) {
    return ResponseEntity.badRequest()
        .body(new ApiError("invalid_request", exception.getMessage(), List.of()));
  }

  @ExceptionHandler(OnlineAuctionNotFoundException.class)
  ResponseEntity<ApiError> handleNotFound(OnlineAuctionNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ApiError("not_found", exception.getMessage(), List.of()));
  }

  private ApiFieldError toFieldError(FieldError fieldError) {
    return new ApiFieldError(fieldError.getField(), fieldError.getDefaultMessage());
  }
}
