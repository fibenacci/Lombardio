import type { CustomerCommandPort } from "../../domain/ports/customer-command.port";
import type { UpdateCustomerAmlCommand } from "../dto/update-customer-aml.command";

export function createUpdateCustomerAmlService(customerCommandPort: CustomerCommandPort) {
  return {
    execute(command: UpdateCustomerAmlCommand) {
      return customerCommandPort.saveAml(
        command.tenantId,
        command.customerId,
        command.payload,
        command.token
      );
    }
  };
}
