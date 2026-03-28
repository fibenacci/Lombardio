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
package io.lombardio.pawnticket.domain.port;

import io.lombardio.pawnticket.domain.model.PawnTicket;
import java.util.List;
import java.util.Optional;

public interface PawnTicketRepository {

  PawnTicket save(PawnTicket pawnTicket);

  Optional<PawnTicket> findByTicketNumber(String ticketNumber);

  List<PawnTicket> findByTenantId(String tenantId);

  List<PawnTicket> findByTenantIdAndCustomerId(String tenantId, String customerId);
}
