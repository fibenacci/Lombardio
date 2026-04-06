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
