import * as customerPortalApi from "../api/customer-portal.api";

export function createHttpCustomerPortalAdapter() {
  return {
    fetchCustomerPortalDocument(ticketNumber: string, token: string) {
      return customerPortalApi.fetchCustomerPortalDocument(ticketNumber, token);
    },
    fetchCustomerPortalPawnTickets(token: string) {
      return customerPortalApi.fetchCustomerPortalPawnTickets(token);
    },
    fetchPortalInvitation(token: string) {
      return customerPortalApi.fetchPortalInvitation(token);
    }
  };
}
