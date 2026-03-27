package io.lombardio.identity.portal.infrastructure.ticket;

import io.lombardio.identity.portal.api.CustomerPortalPawnTicketResponse;

import java.util.List;

public interface CustomerPortalTicketClient {

    List<CustomerPortalPawnTicketResponse> listTickets(String tenantId, String customerId);

    byte[] downloadDocument(String tenantId, String customerId, String ticketNumber);
}
