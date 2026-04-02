import * as accessApi from "../../../../modules/access-management/infrastructure/api/access.api";

export function createHttpUsersAdapter() {
  return {
    createUser(tenantId: string, payload: object) {
      return accessApi.createUser(tenantId, payload);
    },
    fetchBranches(tenantId: string) {
      return accessApi.fetchBranches(tenantId);
    },
    fetchRoles(tenantId: string) {
      return accessApi.fetchRoles(tenantId);
    },
    fetchUsers(tenantId: string) {
      return accessApi.fetchUsers(tenantId);
    },
    updateUser(id: string, payload: object) {
      return accessApi.updateUser(id, payload);
    }
  };
}
