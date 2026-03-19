package io.lombardio.pawnticket.domain.port;

import io.lombardio.pawnticket.domain.model.PawnTicket;

import java.util.List;
import java.util.Optional;

public interface PawnTicketRepository {

    PawnTicket save(PawnTicket pawnTicket);

    Optional<PawnTicket> findByTicketNumber(String ticketNumber);

    List<PawnTicket> findByTenantId(String tenantId);
}
