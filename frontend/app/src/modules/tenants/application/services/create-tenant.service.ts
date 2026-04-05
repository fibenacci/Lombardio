type CreateTenantPayload = {
  key: string;
  displayName: string;
  status: string;
};

type TenantSummary = {
  id: string;
  key: string;
  displayName: string;
  status: string;
};

export function createCreateTenantService(adapter: { createTenant: (payload: CreateTenantPayload) => Promise<TenantSummary> }) {
  return {
    async execute(payload: CreateTenantPayload) {
      return adapter.createTenant(payload);
    }
  };
}
