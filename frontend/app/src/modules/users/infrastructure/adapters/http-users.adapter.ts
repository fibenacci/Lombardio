import * as accessApi from "../../../../modules/access-management/infrastructure/api/access.api";

export function createHttpUsersAdapter() {
  return {
    createUser(tenantId: string, payload: object, token: string) {
      return accessApi.createUser(tenantId, payload, token);
    },
    fetchBranches(tenantId: string, token: string) {
      return accessApi.fetchBranches(tenantId, token);
    },
    fetchRoles(tenantId: string, token: string) {
      return accessApi.fetchRoles(tenantId, token);
    },
    fetchUsers(tenantId: string, token: string) {
      return accessApi.fetchUsers(tenantId, token);
    },
    updateUser(id: string, payload: object, token: string) {
      return accessApi.updateUser(id, payload, token);
    }
  };
}
