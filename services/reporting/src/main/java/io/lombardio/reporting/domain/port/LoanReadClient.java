package io.lombardio.reporting.domain.port;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface LoanReadClient {

    List<ReportedLoanCase> listLoans(String tenantId, String bearerToken);

    record ReportedLoanCase(
            String id,
            Instant recordedAt,
            List<ReportedLoanPosition> positions,
            List<ReportedPawnTicket> pawnTickets
    ) {
    }

    record ReportedLoanPosition(
            String label,
            String guidelineLabel,
            BigDecimal pledgedValue
    ) {
    }

    record ReportedPawnTicket(
            String ticketNumber,
            BigDecimal totalLoanValue
    ) {
    }
}
