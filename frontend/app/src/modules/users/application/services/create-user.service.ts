export function createCreateUserService(adapter: { 
  createUser: (tenantId: string, payload: object) => Promise<any> 
}) {
  return {
    async execute(tenantId: string, payload: any) {
      return adapter.createUser(tenantId, payload);
    }
  };
}
