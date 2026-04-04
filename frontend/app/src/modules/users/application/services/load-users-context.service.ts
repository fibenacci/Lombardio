export function createLoadUsersContextService(adapter: { 
  fetchUsers: (tenantId: string) => Promise<any[]>,
  fetchRoles: (tenantId: string) => Promise<any[]>,
  fetchBranches: (tenantId: string) => Promise<any[]>
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
