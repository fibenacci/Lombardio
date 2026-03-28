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

import io.lombardio.auction.application.service.AuctionNotFoundException;
import io.lombardio.platform.security.ForbiddenException;
import io.lombardio.platform.security.UnauthorizedException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(IllegalArgumentException.class)
  ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException exception) {
    return ResponseEntity.badRequest()
        .body(new ApiError("validation_error", exception.getMessage(), List.of()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
    List<ApiFieldError> fieldErrors =
        exception.getBindingResult().getFieldErrors().stream().map(this::toFieldError).toList();
    return ResponseEntity.badRequest()
        .body(new ApiError("validation_error", "Validation failed", fieldErrors));
  }

  @ExceptionHandler(AuctionNotFoundException.class)
  ResponseEntity<ApiError> handleNotFound(AuctionNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ApiError("not_found", exception.getMessage(), List.of()));
  }

  @ExceptionHandler(UnauthorizedException.class)
  ResponseEntity<ApiError> handleUnauthorized(UnauthorizedException exception) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(new ApiError("unauthorized", exception.getMessage(), List.of()));
  }

  @ExceptionHandler(ForbiddenException.class)
  ResponseEntity<ApiError> handleForbidden(ForbiddenException exception) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(new ApiError("forbidden", exception.getMessage(), List.of()));
  }

  private ApiFieldError toFieldError(FieldError fieldError) {
    return new ApiFieldError(fieldError.getField(), fieldError.getDefaultMessage());
  }
}
