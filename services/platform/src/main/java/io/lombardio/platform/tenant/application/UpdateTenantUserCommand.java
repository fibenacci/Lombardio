package io.lombardio.platform.tenant.application;

import java.util.List;

public record UpdateTenantUserCommand(
    String email, String displayName, String status, List<String> roles, List<String> branchIds) {

  public UpdateTenantUserCommand {
    roles = List.copyOf(roles != null ? roles : List.of());
    branchIds = List.copyOf(branchIds != null ? branchIds : List.of());
  }
}
