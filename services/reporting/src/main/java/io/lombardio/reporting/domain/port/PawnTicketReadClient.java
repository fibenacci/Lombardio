package io.lombardio.reporting.domain.port;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface PawnTicketReadClient {

    List<ReportedPawnTicketOverview> listTickets(String tenantId, String bearerToken);

    List<ReportedCashTransaction> listCashTransactions(String tenantId, String bearerToken);

    record ReportedPawnTicketOverview(
            String ticketNumber,
            BigDecimal totalLoanValue,
            BigDecimal totalRepaymentAmount,
            Integer positionCount
    ) {
    }

    record ReportedCashTransaction(
            String type,
            BigDecimal interestAmount,
            BigDecimal operatingFeeAmount,
            BigDecimal totalAmount,
            Instant createdAt
    ) {
    }
}
