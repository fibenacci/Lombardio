import * as accessApi from "../../../../modules/access-management/infrastructure/api/access.api";

export function createHttpRolesAdapter() {
  return {
    fetchRoles(tenantId: string) {
      return accessApi.fetchRoles(tenantId);
    }
  };
}
