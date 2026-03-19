package io.lombardio.loanorigination.api.http;

import java.util.List;

public record LoanCaseResponse(
        String id,
        CustomerView customer,
        PledgeRecordResponse pledgeRecord,
        List<LoanPositionResponse> positions,
        List<PawnTicketResponse> pawnTickets
) {
}
