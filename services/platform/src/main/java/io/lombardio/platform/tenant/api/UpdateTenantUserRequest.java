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
package io.lombardio.platform.tenant.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.List;

public record UpdateTenantUserRequest(
    @NotBlank @Email String email,
    @NotBlank String displayName,
    @Pattern(regexp = "ACTIVE|INACTIVE", message = "status must be ACTIVE or INACTIVE")
        String status,
    List<String> roles,
    List<String> branchIds) {

  public UpdateTenantUserRequest {
    status = status == null ? null : status.trim();
    roles = List.copyOf(roles != null ? roles : List.of());
    branchIds = List.copyOf(branchIds != null ? branchIds : List.of());
  }
}
