package io.lombardio.identity.portal.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record CustomerPortalPawnTicketResponse(
        String contractNumber,
        String ticketNumber,
        String contractBarcode,
        Instant createdAt,
        LocalDate dueDate,
        LocalDate earliestAuctionDate,
        BigDecimal loanAmount,
        BigDecimal totalRepaymentAmount,
        Integer positionCount
) {
}
