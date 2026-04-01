import type { CustomerCommandPort } from "../../domain/ports/customer-command.port";
import type { UpdateCustomerKycCommand } from "../dto/update-customer-kyc.command";

export function createUpdateCustomerKycService(customerCommandPort: CustomerCommandPort) {
  return {
    execute(command: UpdateCustomerKycCommand) {
      return customerCommandPort.saveKyc(
        command.tenantId,
        command.customerId,
        command.payload,
        command.token
      );
    }
  };
}
