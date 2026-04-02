import * as customerPortalApi from "../api/customer-portal.api";

export function createHttpCustomerPortalAdapter() {
  return {
    fetchCustomerPortalDocument(ticketNumber: string) {
      return customerPortalApi.fetchCustomerPortalDocument(ticketNumber);
    },
    fetchCustomerPortalPawnTickets() {
      return customerPortalApi.fetchCustomerPortalPawnTickets();
    },
    fetchPortalInvitation(token: string) {
      return customerPortalApi.fetchPortalInvitation(token);
    }
  };
}
