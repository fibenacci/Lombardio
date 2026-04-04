import type { CustomerQueryPort } from "../../domain/ports/customer-query.port";
import type { CustomerDetailQuery } from "../dto/customer-detail.query";

export function createLoadCustomerDetailService(customerQueryPort: CustomerQueryPort) {
  return {
    execute(query: CustomerDetailQuery) {
      return customerQueryPort.loadCustomerDetailData(
        query.tenantId,
        query.customerId,
        query.amlEnabled
      );
    }
  };
}
