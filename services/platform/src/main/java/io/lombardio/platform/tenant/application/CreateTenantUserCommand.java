package io.lombardio.platform.tenant.application;

import java.util.List;

public record CreateTenantUserCommand(
    String email, String password, String displayName, List<String> roles, List<String> branchIds) {

  public CreateTenantUserCommand {
    roles = List.copyOf(roles != null ? roles : List.of());
    branchIds = List.copyOf(branchIds != null ? branchIds : List.of());
  }
}
