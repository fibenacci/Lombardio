type UserUpdatePayload = {
  tenantId: string;
  username: string;
  password: string;
  email: string;
  displayName: string;
  status: string;
  roleIds: string[];
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

export function createUpdateUserService(adapter: {
  updateUser: (userId: string, payload: UserUpdatePayload) => Promise<UserSummary>
}) {
  return {
    async execute(userId: string, payload: UserUpdatePayload) {
      return adapter.updateUser(userId, payload);
    }
  };
}
