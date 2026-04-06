package io.lombardio.platform.iam.application;

import io.lombardio.platform.tenant.application.TenantUserView;
import java.util.List;

public interface IdentityAdministration {

  String createTenantGroup(String tenantId, String displayName);

  TenantUserView createTenantUser(
      String tenantId,
      String email,
      String password,
      String displayName,
      List<String> roles,
      List<String> branchIds);

  List<String> getAvailableRoles();

  List<TenantUserView> listTenantUsers(String tenantId);

  TenantUserView updateTenantUser(
      String tenantId,
      String userId,
      String email,
      String displayName,
      String status,
      List<String> roles,
      List<String> branchIds);

  boolean canReachAdminApi();
}
