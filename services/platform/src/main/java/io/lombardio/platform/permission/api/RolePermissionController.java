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
package io.lombardio.platform.permission.api;

import io.lombardio.platform.iam.application.KeycloakService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/platform/operator/permissions")
public class RolePermissionController {

  private final KeycloakService keycloakService;

  public RolePermissionController(KeycloakService keycloakService) {
    this.keycloakService = keycloakService;
  }

  @GetMapping("/roles")
  @PreAuthorize("hasAuthority('platform.tenants.read')")
  public List<String> listRoles() {
    return keycloakService.getAvailableRoles();
  }
}
