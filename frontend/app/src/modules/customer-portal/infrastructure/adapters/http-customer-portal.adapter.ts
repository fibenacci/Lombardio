import * as customerPortalApi from "../api/customer-portal.api";

type PortalInvitation = {
  email?: string;
  firstName?: string;
  lastName?: string;
  expiresAt?: string;
} | null;

export function createHttpCustomerPortalAdapter() {
  return {
    fetchCustomerPortalDocument(ticketNumber: string) {
      return customerPortalApi.fetchCustomerPortalDocument(ticketNumber);
    },
    fetchCustomerPortalPawnTickets() {
      return customerPortalApi.fetchCustomerPortalPawnTickets();
    },
    fetchPortalInvitation(token: string): Promise<PortalInvitation> {
      return customerPortalApi.fetchPortalInvitation(token) as Promise<PortalInvitation>;
    }
  };
}
