import type { CustomerListQuery } from "../dto/customer-list.query";

export function createLoadCustomerListService(
  adapter: {
    fetchAmlStatus: (tenantId: string, customerId: string) => Promise<Record<string, unknown>>;
    searchCustomers: (tenantId: string, query: string) => Promise<Array<Record<string, unknown>>>;
  }
) {
  return async function loadCustomerList(query: CustomerListQuery, t: (key: string) => string) {
    const customers = await adapter.searchCustomers(query.tenantId, query.query);

    return Promise.all(
      customers.map(async (customer) => {
        try {
          const aml = await adapter.fetchAmlStatus(query.tenantId, String(customer.id ?? ""));
          return {
            ...customer,
            amlDecisionReason: aml.decisionReason ?? null,
            amlOriginationAllowed: Boolean(aml.originationAllowed),
            amlRiskLevel: aml.riskLevel ?? null,
            amlStatus: aml.status ?? "UNKNOWN",
            goamlReference: aml.goamlReference ?? null,
            suspiciousActivityReported: Boolean(aml.suspiciousActivityReported)
          };
        } catch {
          return {
            ...customer,
            amlDecisionReason: t("customers.amlLoadFailed"),
            amlOriginationAllowed: false,
            amlRiskLevel: null,
            amlStatus: "UNKNOWN",
            goamlReference: null,
            suspiciousActivityReported: false
          };
        }
      })
    );
  };
}
