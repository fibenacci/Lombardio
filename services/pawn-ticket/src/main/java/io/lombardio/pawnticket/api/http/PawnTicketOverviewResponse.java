package io.lombardio.pawnticket.api.http;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record PawnTicketOverviewResponse(
        String contractNumber,
        String ticketNumber,
        String contractBarcode,
        String termsVersion,
        String customerNumber,
        String customerDisplayName,
        Instant createdAt,
        LocalDate dueDate,
        LocalDate earliestAuctionDate,
        BigDecimal totalLoanValue,
        BigDecimal totalRepaymentAmount,
        Integer positionCount
) {
}
