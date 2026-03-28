/*
 * Lombardio Source-Available No-Distribution License 1.0
 *
 * Copyright (c) 2026 Benjamin Letzel. All rights reserved.
 *
 * This project is source-available for educational and review purposes only.
 * Redistribution, sublicensing, or commercial use is strictly prohibited.
 *
 * For partnership or cooperation inquiries, please contact the author.
 */
package io.lombardio.identity.portal.infrastructure.ticket;

import io.lombardio.identity.portal.api.CustomerPortalPawnTicketResponse;
import java.util.List;

public interface CustomerPortalTicketClient {

  List<CustomerPortalPawnTicketResponse> listTickets(String tenantId, String customerId);

  byte[] downloadDocument(String tenantId, String customerId, String ticketNumber);
}
