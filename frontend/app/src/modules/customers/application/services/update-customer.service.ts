import type { CustomerCommandPort } from "../../domain/ports/customer-command.port";
import type { UpdateCustomerCommand } from "../dto/update-customer.command";

export function createUpdateCustomerService(customerCommandPort: CustomerCommandPort) {
  return {
    execute(command: UpdateCustomerCommand) {
      return customerCommandPort.saveCustomer(
        command.tenantId,
        command.customerId,
        command.payload
      );
    }
  };
}
