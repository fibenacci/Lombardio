package io.lombardio.pawnticket.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public record CashTransaction(
        String id,
        String tenantId,
        String ticketNumber,
        String customerNumber,
        String customerDisplayName,
        CashTransactionType type,
        BigDecimal outstandingLoanAmount,
        BigDecimal interestAmount,
        BigDecimal operatingFeeAmount,
        BigDecimal totalAmount,
        String legalText,
        String note,
        Instant createdAt
) {
}
