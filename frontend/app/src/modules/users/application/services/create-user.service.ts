type UserPayload = {
  email: string;
  password: string;
  displayName: string;
  roles: string[];
  branchIds: string[];
};

type UserSummary = {
  id: string;
  email: string;
  displayName: string;
  status?: string;
  username?: string;
  roleIds?: string[];
  branchIds?: string[];
};

export function createCreateUserService(adapter: {
  createUser: (tenantId: string, payload: UserPayload) => Promise<UserSummary>
}) {
  return {
    async execute(tenantId: string, payload: UserPayload) {
      return adapter.createUser(tenantId, payload);
    }
  };
}
