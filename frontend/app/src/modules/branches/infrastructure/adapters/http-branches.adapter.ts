import * as accessApi from "../../../../modules/access-management/infrastructure/api/access.api";

export function createHttpBranchesAdapter() {
  return {
    createBranch(tenantId: string, payload: object, token: string) {
      return accessApi.createBranch(tenantId, payload, token);
    },
    fetchBranches(tenantId: string, token: string) {
      return accessApi.fetchBranches(tenantId, token);
    }
  };
}
