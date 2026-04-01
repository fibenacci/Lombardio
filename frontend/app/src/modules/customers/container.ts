import { createLoadCustomerDetailService } from "./application/services/load-customer-detail.service";
import { createUpdateCustomerAmlService } from "./application/services/update-customer-aml.service";
import { createUpdateCustomerKycService } from "./application/services/update-customer-kyc.service";
import { createUpdateCustomerService } from "./application/services/update-customer.service";
import { createHttpCustomerAdapter } from "./infrastructure/adapters/http-customer.adapter";

export function createCustomerDetailContainer() {
  const adapter = createHttpCustomerAdapter();

  return {
    adapter,
    services: {
      loadCustomerDetail: createLoadCustomerDetailService(adapter),
      updateCustomer: createUpdateCustomerService(adapter),
      updateCustomerAml: createUpdateCustomerAmlService(adapter),
      updateCustomerKyc: createUpdateCustomerKycService(adapter)
    }
  };
}
