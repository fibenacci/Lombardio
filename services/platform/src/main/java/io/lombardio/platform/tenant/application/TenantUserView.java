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
package io.lombardio.platform.tenant.application;

import java.util.List;

public record TenantUserView(
    String id,
    String username,
    String email,
    String displayName,
    String status,
    List<String> roleIds,
    List<String> branchIds) {

  public TenantUserView {
    roleIds = List.copyOf(roleIds != null ? roleIds : List.of());
    branchIds = List.copyOf(branchIds != null ? branchIds : List.of());
  }
}
