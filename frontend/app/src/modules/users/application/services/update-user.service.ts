export function createUpdateUserService(adapter: { 
  updateUser: (userId: string, payload: object) => Promise<any> 
}) {
  return {
    async execute(userId: string, payload: any) {
      return adapter.updateUser(userId, payload);
    }
  };
}
