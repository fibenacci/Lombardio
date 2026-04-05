import * as accessApi from "../../../../modules/access-management/infrastructure/api/access.api";

type UserSummary = {
  id: string;
  email: string;
  displayName: string;
  status?: string;
  username?: string;
  roleIds?: string[];
  branchIds?: string[];
};

type RoleSummary = {
  id: string;
  key: string;
  displayName: string;
  description: string;
  active: boolean;
  permissionKeys: string[];
};

type BranchSummary = {
  id: string;
  displayName: string;
};

export function createHttpUsersAdapter() {
  return {
    createUser(tenantId: string, payload: object): Promise<UserSummary> {
      return accessApi.createUser(tenantId, payload) as Promise<UserSummary>;
    },
    fetchBranches(tenantId: string): Promise<BranchSummary[]> {
      return accessApi.fetchBranches(tenantId) as Promise<BranchSummary[]>;
    },
    fetchRoles(tenantId: string): Promise<RoleSummary[]> {
      return accessApi.fetchRoles(tenantId) as Promise<RoleSummary[]>;
    },
    fetchUsers(tenantId: string): Promise<UserSummary[]> {
      return accessApi.fetchUsers(tenantId) as Promise<UserSummary[]>;
    },
    updateUser(id: string, payload: object): Promise<UserSummary> {
      return accessApi.updateUser(id, payload) as Promise<UserSummary>;
    }
  };
}
