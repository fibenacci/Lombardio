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

export function createLoadUsersContextService(adapter: {
  fetchUsers: (tenantId: string) => Promise<UserSummary[]>,
  fetchRoles: (tenantId: string) => Promise<RoleSummary[]>,
  fetchBranches: (tenantId: string) => Promise<BranchSummary[]>
}) {
  return {
    async execute(tenantId: string) {
      const [users, roles, branches] = await Promise.all([
        adapter.fetchUsers(tenantId),
        adapter.fetchRoles(tenantId),
        adapter.fetchBranches(tenantId)
      ]);
      return { users, roles, branches };
    }
  };
}
