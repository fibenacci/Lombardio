package io.lombardio.loanorigination.domain.model;

import java.util.List;

public record LoanCase(
        String id,
        String tenantId,
        CustomerProfile customer,
        PledgeRecord pledgeRecord,
        List<LoanPosition> positions,
        List<PawnTicket> pawnTickets
) {
}
