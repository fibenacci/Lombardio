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
package io.lombardio.loanorigination.api.http.error;

import java.util.List;

public record ApiError(
    String code, String message, String traceId, List<ApiFieldError> fieldErrors) {

  public ApiError {
    fieldErrors = List.copyOf(fieldErrors == null ? List.of() : fieldErrors);
  }

  @Override
  public List<ApiFieldError> fieldErrors() {
    return List.copyOf(fieldErrors);
  }
}
