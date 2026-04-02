import * as accessApi from "../../../../modules/access-management/infrastructure/api/access.api";

export function createHttpBranchesAdapter() {
  return {
    createBranch(tenantId: string, payload: object) {
      return accessApi.createBranch(tenantId, payload);
    },
    fetchBranches(tenantId: string) {
      return accessApi.fetchBranches(tenantId);
    }
  };
}
