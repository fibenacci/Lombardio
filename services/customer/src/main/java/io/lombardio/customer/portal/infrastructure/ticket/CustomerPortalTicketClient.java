package io.lombardio.customer.portal.infrastructure.ticket;

import io.lombardio.customer.portal.api.CustomerPortalPawnTicketResponse;

import java.util.List;

public interface CustomerPortalTicketClient {

    List<CustomerPortalPawnTicketResponse> listTickets(String tenantId, String customerId);

    byte[] downloadDocument(String tenantId, String customerId, String ticketNumber);
}
